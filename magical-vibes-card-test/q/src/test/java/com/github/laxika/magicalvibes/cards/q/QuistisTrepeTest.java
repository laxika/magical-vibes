package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AbzanCharm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.f.FireMagic;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuistisTrepe.class, Shock.class, GrizzlyBears.class, Fireball.class, FireMagic.class,
        HillGiant.class, AbzanCharm.class})
class QuistisTrepeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets instant or sorcery cards from either graveyard")
    void etbTargetsEitherGraveyard() {
        Shock ownShock = new Shock();
        Shock opposingShock = new Shock();
        GrizzlyBears opposingBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownShock));
        harness.setGraveyard(player2, List.of(opposingShock, opposingBears));
        harness.setHand(player1, List.of(new QuistisTrepe()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownShock.getId(), opposingShock.getId());
    }

    @Test
    @DisplayName("Casts the chosen spell with any mana and exiles it after resolution")
    void castsWithAnyManaAndExilesAfterResolution() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        harness.setHand(player1, List.of(new QuistisTrepe()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(shock.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }

    @Test
    void decliningLeavesTheSpellInItsOwnersGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        harness.setHand(player1, List.of(new QuistisTrepe()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(shock);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unpayableSpellRemainsInItsOwnersGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        harness.setHand(player1, List.of(new QuistisTrepe()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void paidGraveyardCastAllowsChoosingX() {
        Fireball fireball = new Fireball();
        harness.setGraveyard(player1, List.of(fireball));
        harness.setHand(player1, List.of(new QuistisTrepe()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(fireball.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
    }

    @Test
    void tieredSpellRequiresChoosingAModeBeforeItIsCast() {
        FireMagic fireMagic = new FireMagic();
        harness.setGraveyard(player1, List.of(fireMagic));
        harness.setHand(player1, List.of(new QuistisTrepe()));
        harness.addMana(player1, ManaColor.BLUE, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(fireMagic.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(fireMagic.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
    }

    @Test
    void chosenTierAddsItsCostToTheGraveyardCast() {
        FireMagic fireMagic = new FireMagic();
        harness.setGraveyard(player1, List.of(fireMagic));
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new QuistisTrepe()));
        harness.addMana(player1, ManaColor.BLUE, 9);

        castFromGraveyardWithMode(fireMagic, "Firaga");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(fireMagic.getId()));
    }

    @Test
    void unaffordableTierLeavesTheSpellInTheGraveyard() {
        FireMagic fireMagic = new FireMagic();
        harness.setGraveyard(player1, List.of(fireMagic));
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new QuistisTrepe()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        castFromGraveyardWithMode(fireMagic, "Firaga");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(fireMagic.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void cheapestTierCastsForTheManaCostAlone() {
        FireMagic fireMagic = new FireMagic();
        harness.setGraveyard(player1, List.of(fireMagic));
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new QuistisTrepe()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        castFromGraveyardWithMode(fireMagic, "Fire -");
        harness.passBothPriorities();

        Permanent giant = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Hill Giant")).findFirst().orElseThrow();
        assertThat(giant.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void targetedModeChosenFromTheGraveyardTakesItsTarget() {
        AbzanCharm charm = new AbzanCharm();
        harness.setGraveyard(player1, List.of(charm));
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new QuistisTrepe()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        castFromGraveyardWithMode(charm, "Exile target creature");
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(giant.getCard().getId()));
    }

    @Test
    void modeWithoutALegalTargetLeavesTheSpellInTheGraveyard() {
        AbzanCharm charm = new AbzanCharm();
        harness.setGraveyard(player1, List.of(charm));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuistisTrepe()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        castFromGraveyardWithMode(charm, "Exile target creature");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(charm.getId()));
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castFromGraveyardWithMode(Card spell, String modeLabel) {
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.ColorChoice modeChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, modeChoice.options().stream()
                .filter(option -> option.startsWith(modeLabel)).findFirst().orElseThrow());
    }
}
