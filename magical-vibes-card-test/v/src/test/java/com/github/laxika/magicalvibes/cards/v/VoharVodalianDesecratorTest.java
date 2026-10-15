package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoharVodalianDesecrator.class, CounselOfTheSoratami.class, GrizzlyBears.class, Shock.class})
class VoharVodalianDesecratorTest extends BaseCardTest {

    @Test
    @DisplayName("Looting an instant or sorcery drains each opponent and gains life")
    void lootingSpellDrainsOpponents() {
        Permanent vohar = addReadyVohar();
        Shock discarded = new Shock();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, indexOfVohar(vohar), 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Looting a nonspell card does not drain")
    void lootingNonspellDoesNotDrain() {
        Permanent vohar = addReadyVohar();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, indexOfVohar(vohar), 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The sacrifice ability grants a later targeted graveyard cast and exiles the spell")
    void grantsLaterGraveyardCast() {
        Permanent vohar = addReadyVohar();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOfVohar(vohar), 1, null, counsel.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vohar);
        harness.assertInGraveyard(player1, "Counsel of the Soratami");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromGraveyard(player1, counsel.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Counsel of the Soratami");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(counsel);
    }

    @Test
    @DisplayName("The sacrifice ability targets only instant or sorcery cards")
    void sacrificeAbilityRejectsCreatureTarget() {
        Permanent vohar = addReadyVohar();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOfVohar(vohar), 1, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The sacrifice ability can only be activated at sorcery speed")
    void sacrificeAbilityRequiresSorcerySpeed() {
        Permanent vohar = addReadyVohar();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOfVohar(vohar), 1, null, counsel.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discarding a sorcery changes life during the original ability resolution")
    void sorceryDiscardDrainsBeforePlayersReceivePriority() {
        Permanent vohar = addReadyVohar();
        CounselOfTheSoratami discarded = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, indexOfVohar(vohar), 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Counsel of the Soratami");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty hand still discards the newly drawn instant and drains immediately")
    void lootingFromEmptyHandDiscardsDrawnCard() {
        Permanent vohar = addReadyVohar();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, indexOfVohar(vohar), 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Shock");
        harness.assertNotInHand(player1, "Shock");
        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Vohar can be sacrificed while tapped and summoning sick")
    void sacrificeDoesNotRequireUntappedOrReadyCreature() {
        Permanent vohar = harness.addToBattlefieldAndReturn(player1, new VoharVodalianDesecrator());
        vohar.tap();
        vohar.setSummoningSick(true);
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOfVohar(vohar), 1, null, counsel.getId(), Zone.GRAVEYARD);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vohar);
        harness.assertInGraveyard(player1, "Vohar, Vodalian Desecrator");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Counsel of the Soratami");
    }

    @Test
    @DisplayName("The sacrifice ability cannot target an opponent's graveyard")
    void sacrificeAbilityRejectsOpponentsGraveyard() {
        Permanent vohar = addReadyVohar();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player2, List.of(counsel));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOfVohar(vohar), 1, null, counsel.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted graveyard spell still requires its mana cost")
    void grantedSpellRequiresMana() {
        Permanent vohar = addReadyVohar();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOfVohar(vohar), 1, null, counsel.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, counsel.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Counsel of the Soratami");
    }

    @Test
    @DisplayName("The granted sorcery cannot be cast during combat")
    void grantedSorceryRetainsNormalTiming() {
        Permanent vohar = addReadyVohar();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOfVohar(vohar), 1, null, counsel.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, counsel.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Counsel of the Soratami");
    }
    @Test
    @DisplayName("The granted instant can be cast during combat and is then exiled")
    void grantedInstantCanBeCastDuringCombat() {
        Permanent vohar = addReadyVohar();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOfVohar(vohar), 1, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castFromGraveyardTargeting(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(shock), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    @DisplayName("An unused graveyard casting permission expires at the end of the turn")
    void graveyardPermissionExpires() {
        Permanent vohar = addReadyVohar();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOfVohar(vohar), 1, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(shock), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(shock);
    }
    private Permanent addReadyVohar() {
        return addCreatureReady(player1, new VoharVodalianDesecrator());
    }

    private int indexOfVohar(Permanent vohar) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(vohar);
    }
}
