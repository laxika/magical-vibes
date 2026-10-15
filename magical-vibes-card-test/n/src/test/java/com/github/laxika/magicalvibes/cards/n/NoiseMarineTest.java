package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoiseMarine.class, GrizzlyBears.class, LightningBolt.class, Mountain.class})
class NoiseMarineTest extends BaseCardTest {

    @Test
    @DisplayName("Cascade offers a lesser nonland card from the top of the library")
    void cascadeOffersLesserNonlandCard() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new NoiseMarine()));
        addNoiseMarineMana();

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).extracting(Card::getName).containsExactly("Grizzly Bears");

        harness.handleCardChosen(player1, -1);
    }

    @Test
    @DisplayName("The Sonic Blaster trigger deals damage equal to spells cast this turn")
    void etbDealsDamageEqualToSpellsCastThisTurn() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new NoiseMarine()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0, player2.getId());
        resolveAllStack();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Casting the cascade hit increases Sonic Blaster's damage")
    void cascadeSpellCountsTowardDamage() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new NoiseMarine()));
        addNoiseMarineMana();

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        resolveAllStack();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Noise Marine");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cascade skips lands and cards with equal mana value, and declining bottoms them")
    void cascadeSkipsLandsAndEqualManaValue() {
        Mountain land = new Mountain();
        NoiseMarine equalManaValue = new NoiseMarine();
        GrizzlyBears hit = new GrizzlyBears();
        LightningBolt untouched = new LightningBolt();
        harness.setLibrary(player1, List.of(land, equalManaValue, hit, untouched));
        harness.setHand(player1, List.of(new NoiseMarine()));
        addNoiseMarineMana();

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(land, equalManaValue, hit);
        assertThat(gd.findExiledCard(land.getId())).isNull();
        assertThat(gd.findExiledCard(equalManaValue.getId())).isNull();
        assertThat(gd.findExiledCard(hit.getId())).isNull();
        resolveAllStack();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Cascade with no qualifying card returns the exiled cards and still resolves Noise Marine")
    void noCascadeHitStillResolvesCreature() {
        Mountain land = new Mountain();
        NoiseMarine equalManaValue = new NoiseMarine();
        harness.setLibrary(player1, List.of(land, equalManaValue));
        harness.setHand(player1, List.of(new NoiseMarine()));
        addNoiseMarineMana();

        harness.castCreature(player1, 0, player2.getId());
        resolveAllStack();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, equalManaValue);
        assertThat(gd.findExiledCard(land.getId())).isNull();
        assertThat(gd.findExiledCard(equalManaValue.getId())).isNull();
        harness.assertOnBattlefield(player1, "Noise Marine");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Sonic Blaster counts spells cast in response to its trigger")
    void damageCountIsEvaluatedAtResolution() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new NoiseMarine(), new LightningBolt()));
        addNoiseMarineMana();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Noise Marine");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllStack();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Sonic Blaster survives removal and does not count an opponent's spell")
    void sourceRemovalDoesNotStopTriggerOrCountOpponentSpell() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new NoiseMarine()));
        harness.setHand(player2, List.of(new LightningBolt()));
        addNoiseMarineMana();
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Noise Marine"));
        harness.assertInGraveyard(player1, "Noise Marine");
        resolveAllStack();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Sonic Blaster can damage a creature")
    void sonicBlasterCanTargetCreature() {
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new NoiseMarine()));
        addNoiseMarineMana();

        harness.castCreature(player1, 0, target.getId());
        resolveAllStack();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Entering without being cast deals zero damage when no spells have been cast")
    void enteringWithoutCastingDealsZeroDamage() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.enterBattlefieldAndReturn(player1, new NoiseMarine());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllStack();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Noise Marine");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    private void addNoiseMarineMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void resolveAllStack() {
        for (int i = 0; i < 12 && (!gd.stack.isEmpty() || gd.interaction.isAwaitingInput()); i++) {
            if (gd.interaction.isAwaitingInput()) {
                return;
            }
            harness.passBothPriorities();
        }
    }
}
