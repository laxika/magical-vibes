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

    private Permanent addReadyVohar() {
        return addCreatureReady(player1, new VoharVodalianDesecrator());
    }

    private int indexOfVohar(Permanent vohar) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(vohar);
    }
}
