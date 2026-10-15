package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.b.BuyYourSilence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShatteredSeraph.class, Island.class})
class ShatteredSeraphTest extends BaseCardTest {

    @Test
    void entersAndYouGainThreeLife() {
        harness.castFromHand(player1, new ShatteredSeraph(), "{4}{W}{U}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    void handAbilityExilesTheCardAndGrantsOnlyWhiteBlueOrBlackMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        ShatteredSeraph seraph = new ShatteredSeraph();
        harness.setHand(player1, List.of(seraph));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, land.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(seraph.getId())).isNotNull();

        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("WHITE", "BLUE", "BLACK");
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void landGrantEndsWhenShatteredSeraphIsCastFromExile() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        ShatteredSeraph seraph = new ShatteredSeraph();
        harness.setHand(player1, List.of(seraph));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");
        land.untap();

        harness.castFromExile(player1, seraph.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({ShatteredSeraph.class, BuyYourSilence.class})
    void cannotCastWhenExiledByAnotherEffect() {
        ShatteredSeraph seraph = new ShatteredSeraph();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, seraph);
        harness.setHand(player1, List.of(new BuyYourSilence()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        assertThat(gd.findExiledCard(seraph.getId())).isNotNull();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, seraph.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(seraph.getId())).isNotNull();
    }

    @Test
    void canGrantManaAbilityToOpponentsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ShatteredSeraph()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLACK");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void insufficientManaDoesNotExileCard() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        ShatteredSeraph seraph = new ShatteredSeraph();
        harness.setHand(player1, List.of(seraph));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(seraph);
        assertThat(gd.findExiledCard(seraph.getId())).isNull();
    }

    @Test
    void castingFromExileResolvesAndGainsLife() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        ShatteredSeraph seraph = new ShatteredSeraph();
        harness.setHand(player1, List.of(seraph));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.castFromExile(player1, seraph.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shattered Seraph");
        harness.assertLife(player1, 23);
        assertThat(gd.findExiledCard(seraph.getId())).isNull();
    }

    @Test
    void cannotTargetNonlandPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ShatteredSeraph());
        ShatteredSeraph seraph = new ShatteredSeraph();
        harness.setHand(player1, List.of(seraph));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(seraph);
        assertThat(gd.findExiledCard(seraph.getId())).isNull();
    }
}
