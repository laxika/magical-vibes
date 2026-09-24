package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InallaArchmageRitualist.class, FugitiveWizard.class})
class InallaArchmageRitualistTest extends BaseCardTest {

    @Test
    @DisplayName("Eminence creates a hasty Wizard token copy from the command zone")
    void commandZoneEminenceCreatesHastyExilingTokenCopy() {
        InallaArchmageRitualist inalla = new InallaArchmageRitualist();
        addToCommandZone(player1, inalla);
        castWizard();

        assertThat(findPermanents(player1, "Fugitive Wizard")).hasSize(2);
        Permanent token = findPermanents(player1, "Fugitive Wizard").stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(token.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP));
    }

    @Test
    @DisplayName("Eminence works while Inalla is on the battlefield")
    void battlefieldEminenceCreatesTokenCopy() {
        addCreatureReady(player1, new InallaArchmageRitualist());
        castWizard();

        assertThat(findPermanents(player1, "Fugitive Wizard")).hasSize(2);
    }

    @Test
    @DisplayName("A command-zone Eminence trigger does nothing after Inalla leaves the command zone")
    void commandZoneTriggerChecksSourceStillInCommandZone() {
        InallaArchmageRitualist inalla = new InallaArchmageRitualist();
        addToCommandZone(player1, inalla);
        prepareWizardCast();

        gd.playerCommandZones.get(player1.getId()).remove(inalla);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Fugitive Wizard")).hasSize(1);
    }

    @Test
    @DisplayName("Five untapped Wizards can make a player lose 7 life")
    void tapsFiveWizardsToMakePlayerLoseLife() {
        Permanent inalla = addCreatureReady(player1, new InallaArchmageRitualist());
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player1, new FugitiveWizard());
        }

        int inallaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(inalla);
        harness.activateAbility(player1, inallaIndex, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTapped)
                .count()).isEqualTo(5);
    }

    private void castWizard() {
        castWizard(true);
    }

    private void castWizard(boolean acceptMay) {
        prepareWizardCast();
        harness.handleMayAbilityChosen(player1, acceptMay);
    }

    private void prepareWizardCast() {
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addToCommandZone(Player player, Card card) {
        gd.playerCommandZones.get(player.getId()).add(card);
    }
}
