package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CelestineCaveWitch.class, DeadlyInsect.class})
class CelestineCaveWitchTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two Insect tokens")
    void entersWithTwoInsects() {
        harness.setHand(player1, List.of(new CelestineCaveWitch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .map(Permanent::getCard)
                .allMatch(card -> card.getSubtypes().contains(CardSubtype.INSECT))).isTrue();
    }

    @Test
    @DisplayName("May sacrifice an Insect to curse the defending player")
    void sacrificesInsectToCreateAttachedCurse() {
        addCreatureReady(player1, new CelestineCaveWitch());
        Permanent insect = harness.addToBattlefieldAndReturn(player1, new DeadlyInsect());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(((PendingInteraction.PermanentChoice) gd.interaction.activeInteraction()).validIds())
                .containsExactly(insect.getId());

        harness.handlePermanentChosen(player1, insect.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(insect.getCard());
        Permanent curse = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.CURSE))
                .findFirst().orElseThrow();
        assertThat(curse.getCard().getSubtypes()).contains(CardSubtype.AURA, CardSubtype.CURSE);
        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());

        int lifeBeforeUpkeep = gd.playerLifeTotals.get(player2.getId());
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBeforeUpkeep - 1);
    }

    @Test
    @DisplayName("Does not offer the sacrifice without an Insect")
    void doesNotOfferSacrificeWithoutInsect() {
        addCreatureReady(player1, new CelestineCaveWitch());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
