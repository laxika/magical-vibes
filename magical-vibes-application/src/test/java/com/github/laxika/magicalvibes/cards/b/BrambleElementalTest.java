package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.MoldervineCloak;
import com.github.laxika.magicalvibes.cards.p.Putrefy;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrambleElemental.class, MoldervineCloak.class, Putrefy.class})
class BrambleElementalTest extends BaseCardTest {

    @Test
    void auraAttachedToBrambleElementalCreatesTwoSaprolings() {
        Permanent elemental = addCreatureReady(player1, new BrambleElemental());

        attachMoldervineCloak(player1, elemental);

        assertThat(findPermanents(player1, "Saproling"))
                .hasSize(2)
                .allSatisfy(saproling -> {
                    assertThat(saproling.getCard().isToken()).isTrue();
                    assertThat(saproling.getCard().getPower()).isEqualTo(1);
                    assertThat(saproling.getCard().getToughness()).isEqualTo(1);
                    assertThat(saproling.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
                });
    }

    @Test
    void opponentAuraAttachedToBrambleElementalCreatesTokensForElementalsController() {
        Permanent elemental = addCreatureReady(player1, new BrambleElemental());

        attachMoldervineCloak(player2, elemental);

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    void auraAttachedToAnotherBrambleElementalDoesNotTriggerThisOne() {
        addCreatureReady(player1, new BrambleElemental());
        Permanent otherElemental = addCreatureReady(player2, new BrambleElemental());

        attachMoldervineCloak(player1, otherElemental);

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(findPermanents(player2, "Saproling")).hasSize(2);
    }

    @Test
    void eachAdditionalAuraCreatesTwoMoreSaprolings() {
        Permanent elemental = addCreatureReady(player1, new BrambleElemental());

        attachMoldervineCloak(player1, elemental);
        attachMoldervineCloak(player1, elemental);

        assertThat(findPermanents(player1, "Saproling")).hasSize(4);
        assertThat(findPermanents(player1, "Moldervine Cloak")).hasSize(2);
    }

    @Test
    void tokensAreCreatedByATriggerAfterTheAuraResolves() {
        Permanent elemental = addCreatureReady(player1, new BrambleElemental());
        prepareMoldervineCloak(player1);

        harness.castEnchantment(player1, 0, elemental.getId());
        assertThat(findPermanents(player1, "Saproling")).isEmpty();

        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Moldervine Cloak").getAttachedTo()).isEqualTo(elemental.getId());
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
    }

    @Test
    void attachmentTriggerStillCreatesTokensAfterElementalIsDestroyed() {
        Permanent elemental = addCreatureReady(player1, new BrambleElemental());
        prepareMoldervineCloak(player1);
        harness.castEnchantment(player1, 0, elemental.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Putrefy()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, elemental.getId());

        assertThat(findPermanents(player1, "Bramble Elemental")).isEmpty();
        assertThat(findPermanents(player1, "Saproling")).isEmpty();

        resolveAllTriggers();
        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    void auraWhoseTargetIsDestroyedBeforeResolutionDoesNotCreateTokens() {
        Permanent elemental = addCreatureReady(player1, new BrambleElemental());
        prepareMoldervineCloak(player1);
        harness.castEnchantment(player1, 0, elemental.getId());

        harness.setHand(player2, List.of(new Putrefy()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, elemental.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bramble Elemental")).isEmpty();
        assertThat(findPermanents(player1, "Moldervine Cloak")).isEmpty();
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    private void attachMoldervineCloak(Player controller, Permanent target) {
        prepareMoldervineCloak(controller);
        harness.castEnchantment(controller, 0, target.getId());
        resolveAllTriggers();
    }

    private void prepareMoldervineCloak(Player controller) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(controller, List.of(new MoldervineCloak()));
        harness.addMana(controller, ManaColor.GREEN, 1);
        harness.addMana(controller, ManaColor.COLORLESS, 2);
    }
}
