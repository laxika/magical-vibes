package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CosmotronicWave;
import com.github.laxika.magicalvibes.cards.d.DampingSphere;
import com.github.laxika.magicalvibes.cards.d.DirectCurrent;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.n.NullhideFerox;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OmnispellAdept.class, Divination.class, GrizzlyBears.class, Shock.class,
        CosmotronicWave.class, DirectCurrent.class, NullhideFerox.class, DampingSphere.class})
class OmnispellAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Offers an instant or sorcery from hand and casts it without paying its mana cost")
    void castsInstantOrSorceryFromHandForFree() {
        Divination spell = new Divination();
        Permanent adept = addCreatureReady(player1, new OmnispellAdept());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(adept.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not offer a creature card")
    void doesNotOfferCreature() {
        addCreatureReady(player1, new OmnispellAdept());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the spell in hand")
    void decliningLeavesSpellInHand() {
        Shock spell = new Shock();
        addCreatureReady(player1, new OmnispellAdept());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new OmnispellAdept());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can cast a sorcery during the opponent's turn")
    void castsSorceryDuringOpponentsTurn() {
        CosmotronicWave spell = new CosmotronicWave();
        addCreatureReady(player1, new OmnispellAdept());
        Permanent opposingCreature = addCreatureReady(player2, new OmnispellAdept());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        harness.passBothPriorities();

        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Cosmotronic Wave");
    }

    @Test
    @DisplayName("Can decline one card and cast another, but only one spell is cast")
    void castsOnlyOneSpellWithChosenTarget() {
        CosmotronicWave first = new CosmotronicWave();
        DirectCurrent second = new DirectCurrent();
        addCreatureReady(player1, new OmnispellAdept());
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(second.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Direct Current");
    }

    @Test
    @DisplayName("Free casting cannot bypass Nullhide Ferox's prohibition")
    void cannotCastNoncreatureSpellWithNullhideFerox() {
        CosmotronicWave spell = new CosmotronicWave();
        addCreatureReady(player1, new OmnispellAdept());
        harness.addToBattlefield(player1, new NullhideFerox());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).contains(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting without paying mana cost still pays Damping Sphere's increase")
    void paysManaCostIncreaseWhenCastingForFree() {
        addCreatureReady(player1, new OmnispellAdept());
        harness.setHand(player1, List.of(new CosmotronicWave()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.addToBattlefield(player2, new DampingSphere());
        CosmotronicWave spell = new CosmotronicWave();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("An instant cast for free still chooses its target")
    void castsInstantWithChosenTarget() {
        addCreatureReady(player1, new OmnispellAdept());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
    }
}
