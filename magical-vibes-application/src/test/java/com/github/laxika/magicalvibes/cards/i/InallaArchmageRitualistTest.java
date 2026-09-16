package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InallaArchmageRitualist.class, FugitiveWizard.class, GrizzlyBears.class})
class InallaArchmageRitualistTest extends BaseCardTest {

    @Test
    void eminenceCopiesAWizardFromTheBattlefield() {
        harness.addToBattlefield(player1, new InallaArchmageRitualist());
        castCreatureWithOneMana(new FugitiveWizard());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Fugitive Wizard")).hasSize(2);
        Permanent token = findPermanents(player1, "Fugitive Wizard").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(token.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP));
    }

    @Test
    void eminenceWorksFromTheCommandZone() {
        Card inalla = new InallaArchmageRitualist();
        gd.playerCommandZones.get(player1.getId()).add(inalla);
        castCreatureWithOneMana(new FugitiveWizard());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Fugitive Wizard")).hasSize(2);
    }

    @Test
    void eminenceDoesNotTriggerForNonWizards() {
        harness.addToBattlefield(player1, new InallaArchmageRitualist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    void activatedAbilityTapsFiveWizardsAndMakesTargetPlayerLoseSevenLife() {
        Permanent inalla = addReady(player1, new InallaArchmageRitualist());
        for (int i = 0; i < 4; i++) {
            addReady(player1, new FugitiveWizard());
        }

        harness.setLife(player2, 20);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(inalla), 0,
                null, player2.getId());

        List<Permanent> wizards = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.WIZARD))
                .filter(permanent -> !permanent.isTapped())
                .limit(5)
                .toList();
        for (Permanent wizard : wizards) {
            harness.handlePermanentChosen(player1, wizard.getId());
        }
        harness.passBothPriorities();

        assertThat(wizards).hasSize(5);
        assertThat(wizards).allMatch(Permanent::isTapped);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    private void castCreatureWithOneMana(Card creature) {
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
