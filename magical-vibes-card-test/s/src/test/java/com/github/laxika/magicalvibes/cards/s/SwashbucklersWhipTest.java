package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PacificationArray;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwashbucklersWhip.class, GrizzlyBears.class, PacificationArray.class, GloriousAnthem.class})
class SwashbucklersWhipTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has reach")
    void equippedCreatureHasReach() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent whip = addWhipReady(player1);
        whip.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Equip {1} attaches the Whip to a creature you control")
    void equipAttachesToCreature() {
        Permanent whip = addWhipReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(whip.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature can tap a target artifact or creature")
    void equippedCreatureCanTapArtifactOrCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent whip = addWhipReady(player1);
        whip.setAttachedTo(creature.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PacificationArray());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equipped creature cannot target an enchantment")
    void equippedCreatureCannotTargetEnchantment() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent whip = addWhipReady(player1);
        whip.setAttachedTo(creature.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Equipped creature can discover 10")
    void equippedCreatureCanDiscoverTen() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent whip = addWhipReady(player1);
        whip.setAttachedTo(creature.getId());
        Card discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(discovered));

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    @Test
    @DisplayName("The tap ability targets creatures and taps the equipped creature as its cost")
    void tapsCreatureAndPaysTapCost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent whip = addWhipReady(player1);
        whip.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(creature.isTapped()).isTrue();
        assertThat(whip.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Discover can cast the discovered creature without paying its mana cost")
    void castsDiscoveredCreatureForFree() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent whip = addWhipReady(player1);
        whip.setAttachedTo(creature.getId());
        Card discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(discovered));

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(creature.isTapped()).isTrue();
        assertThat(whip.isTapped()).isFalse();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == discovered);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == discovered);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discovered);
    }

    @Test
    @DisplayName("Moving the Equipment transfers reach to the newly equipped creature")
    void reequippingTransfersReach() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent whip = addWhipReady(player1);
        whip.setAttachedTo(first.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();

        assertThat(whip.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Summoning sickness prevents activating either granted tap ability")
    void summoningSicknessPreventsGrantedTapAbilities() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent whip = addWhipReady(player1);
        whip.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addWhipReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SwashbucklersWhip());
    }
}
