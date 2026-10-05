package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CallToHeel;
import com.github.laxika.magicalvibes.cards.r.ResoundingThunder;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Kathari Screecher")
@CardUsed({KathariScreecher.class, Terminate.class, CallToHeel.class, ResoundingThunder.class})
class KathariScreecherTest extends BaseCardTest {

    @Test
    @DisplayName("Unearth returns Kathari Screecher to the battlefield with haste")
    void unearthReturnsWithHaste() {
        KathariScreecher card = new KathariScreecher();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Kathari Screecher");
        assertThat(perm.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Kathari Screecher");
    }

    @Test
    @DisplayName("Unearthed Kathari Screecher is exiled at the next end step")
    void unearthExiledAtEndStep() {
        KathariScreecher card = new KathariScreecher();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Kathari Screecher");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kathari Screecher"));
    }

    @Test
    @DisplayName("Unearth can only be activated at sorcery speed")
    void unearthOnlyAtSorcerySpeed() {
        KathariScreecher card = new KathariScreecher();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Kathari Screecher");
    }

    @Test
    @DisplayName("Unearthed Kathari Screecher is exiled if it would leave the battlefield")
    void unearthExiledIfWouldLeaveBattlefield() {
        KathariScreecher card = new KathariScreecher();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Kathari Screecher");

        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, perm.getId());

        harness.assertNotOnBattlefield(player1, "Kathari Screecher");
        harness.assertNotInGraveyard(player1, "Kathari Screecher");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kathari Screecher"));
    }

    @Test
    @DisplayName("An unearthed Kathari Screecher can attack immediately")
    void unearthAllowsAttackingDespiteSummoningSickness() {
        harness.setGraveyard(player1, List.of(new KathariScreecher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(findPermanent(player1, "Kathari Screecher").isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Unearth cannot be activated during combat on its controller's turn")
    void unearthCannotBeActivatedDuringCombat() {
        harness.setGraveyard(player1, List.of(new KathariScreecher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Kathari Screecher");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unearth requires the full mana cost")
    void unearthCannotBeActivatedWithInsufficientMana() {
        harness.setGraveyard(player1, List.of(new KathariScreecher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Kathari Screecher");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unearth cannot be activated in response to a spell")
    void unearthCannotBeActivatedWithSpellOnStack() {
        harness.setGraveyard(player1, List.of(new KathariScreecher()));
        harness.setHand(player1, List.of(new ResoundingThunder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Kathari Screecher");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Returning an unearthed creature to hand exiles it and still draws a card")
    void bounceExilesUnearthedCreatureAndStillDraws() {
        KathariScreecher card = new KathariScreecher();
        KathariScreecher drawnCard = new KathariScreecher();
        harness.setGraveyard(player1, List.of(card));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new CallToHeel()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Kathari Screecher").getId());

        harness.assertNotOnBattlefield(player1, "Kathari Screecher");
        harness.assertNotInGraveyard(player1, "Kathari Screecher");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard).doesNotContain(card);
    }
}
