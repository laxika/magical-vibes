package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MysticSkull.class, Forest.class})
class MysticSkullTest extends BaseCardTest {

    @Test
    void firstAbilityAddsAnyColorMana() {
        Permanent skull = addReadySkull(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, skull), 0, null, null);

        assertThat(skull.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void secondAbilityTransformsTheArtifact() {
        Permanent skull = addReadySkull(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, indexOf(player1, skull), 1, null, null);
        harness.passBothPriorities();

        assertThat(skull.isTransformed()).isTrue();
    }

    @Test
    void transformedFaceGivesControlledLandsAnAnyColorAbility() {
        Permanent skull = addTransformedSkull(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, indexOf(player1, forest), 0, null, null);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(skull.isTransformed()).isTrue();
    }

    @Test
    void transformedFaceDoesNotGiveOpponentsLandsAnAbility() {
        addTransformedSkull(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player2, indexOf(player2, forest), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void newlyEnteredNoncreatureSkullCanProduceMana() {
        Permanent skull = harness.addToBattlefieldAndReturn(player1, new MysticSkull());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, skull), 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(skull.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void manaAbilityCannotBeActivatedWithoutItsGenericManaCost() {
        Permanent skull = addReadySkull(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, skull), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(skull.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void transformAbilityCannotBeActivatedWithOnlyFourMana() {
        Permanent skull = addReadySkull(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, skull), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(skull.isTapped()).isFalse();
        assertThat(skull.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }

    @Test
    void transformationUsesTheStackAndImmediatelyEnablesLandManaWhenItResolves() {
        Permanent skull = harness.addToBattlefieldAndReturn(player1, new MysticSkull());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, indexOf(player1, skull), 1, null, null);

        assertThat(skull.isTapped()).isTrue();
        assertThat(skull.isTransformed()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(skull.isTransformed()).isTrue();
        assertThat(skull.isTapped()).isTrue();
        harness.activateAbility(player1, indexOf(player1, forest), 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, "WHITE");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void landsRetainTheirOriginalManaAbility() {
        addTransformedSkull(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.tapPermanent(player1, indexOf(player1, forest));

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void landAbilityDisappearsWhenMonstrosityLeavesTheBattlefield() {
        Permanent skull = addTransformedSkull(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.playerBattlefields.get(player1.getId()).remove(skull);
        gd.playerGraveyards.get(player1.getId()).add(skull.getOriginalCard());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, forest), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThat(forest.isTapped()).isFalse();

        harness.tapPermanent(player1, indexOf(player1, forest));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private Permanent addReadySkull(Player player) {
        return addCreatureReady(player, new MysticSkull());
    }

    private Permanent addTransformedSkull(Player player) {
        Permanent skull = addReadySkull(player);
        skull.setCard(skull.getOriginalCard().getBackFaceCard());
        skull.setTransformed(true);
        return skull;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
