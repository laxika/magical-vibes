package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BesottedKnight;
import com.github.laxika.magicalvibes.cards.o.OreskosSunGuide;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwistedFealty.class, BesottedKnight.class, OreskosSunGuide.class})
class TwistedFealtyTest extends BaseCardTest {

    @Test
    void stealsUntapsGrantsHasteAndAttachesWickedRole() {
        Permanent target = addCreatureReady(player2, new BesottedKnight());
        target.tap();
        castTwistedFealty(List.of(target.getId(), target.getId()));

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        Permanent role = findPermanent(player1, "Wicked");
        assertThat(role.getCard().isToken()).isTrue();
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    @Test
    void roleTargetMayBeOmitted() {
        Permanent target = addCreatureReady(player2, new BesottedKnight());
        castTwistedFealty(List.of(target.getId()));

        assertThat(findPermanents(player1, "Wicked")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    void controlAndHasteExpireButRoleRemainsAttached() {
        Permanent target = addCreatureReady(player2, new BesottedKnight());
        castTwistedFealty(List.of(target.getId(), target.getId()));
        Permanent role = findPermanent(player1, "Wicked");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    @Test
    void wickedRoleCausesEachOpponentToLoseLifeWhenItDies() {
        Permanent target = addCreatureReady(player2, new BesottedKnight());
        castTwistedFealty(List.of(target.getId(), target.getId()));
        Permanent role = findPermanent(player1, "Wicked");
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, role));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void controlChangesBeforeUntapTriggerIsCreated() {
        Permanent target = addCreatureReady(player2, new OreskosSunGuide());
        target.tap();
        int ownLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        castTwistedFealty(List.of(target.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    void roleCanEnchantADifferentOpponentsCreatureWithoutStealingOrUntappingIt() {
        Permanent stolen = addCreatureReady(player2, new BesottedKnight());
        Permanent enchanted = addCreatureReady(player2, new BesottedKnight());
        stolen.tap();
        enchanted.tap();

        castTwistedFealty(List.of(stolen.getId(), enchanted.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(stolen).doesNotContain(enchanted);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
        assertThat(stolen.isTapped()).isFalse();
        assertThat(stolen.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(enchanted.isTapped()).isTrue();
        assertThat(enchanted.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(findPermanent(player1, "Wicked").getAttachedTo()).isEqualTo(enchanted.getId());
        assertThat(gqs.getEffectivePower(gd, stolen)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(4);
    }

    @Test
    void canUntapAndGrantHasteToOwnCreature() {
        Permanent target = addCreatureReady(player1, new BesottedKnight());
        target.tap();

        castTwistedFealty(List.of(target.getId(), target.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(findPermanent(player1, "Wicked").getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void illegalRoleTargetDoesNotPreventStealingFirstTargetOrCreateRoleOnIt() {
        Permanent stolen = addCreatureReady(player2, new BesottedKnight());
        Permanent enchanted = addCreatureReady(player2, new BesottedKnight());
        stolen.tap();
        prepareTwistedFealty();
        harness.castSorcery(player1, 0, List.of(stolen.getId(), enchanted.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, enchanted));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(stolen);
        assertThat(stolen.isTapped()).isFalse();
        assertThat(stolen.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(findPermanents(player1, "Wicked")).isEmpty();
    }

    @Test
    void illegalFirstTargetDoesNotPreventRoleCreationOnSecondTarget() {
        Permanent stolen = addCreatureReady(player2, new BesottedKnight());
        Permanent enchanted = addCreatureReady(player2, new BesottedKnight());
        enchanted.tap();
        prepareTwistedFealty();
        harness.castSorcery(player1, 0, List.of(stolen.getId(), enchanted.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, stolen));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
        assertThat(enchanted.isTapped()).isTrue();
        assertThat(enchanted.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(findPermanent(player1, "Wicked").getAttachedTo()).isEqualTo(enchanted.getId());
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(4);
    }

    @Test
    void allTargetsIllegalPreventsRoleCreation() {
        Permanent target = addCreatureReady(player2, new BesottedKnight());
        prepareTwistedFealty();
        harness.castSorcery(player1, 0, List.of(target.getId(), target.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wicked")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof TwistedFealty);
    }

    @Test
    void replacingOwnRoleKeepsNewestAndDrainsOpponentOnce() {
        Permanent target = addCreatureReady(player1, new BesottedKnight());
        castTwistedFealty(List.of(target.getId(), target.getId()));
        Permanent firstRole = findPermanent(player1, "Wicked");
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int ownLifeBefore = gd.playerLifeTotals.get(player1.getId());

        castTwistedFealty(List.of(target.getId(), target.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wicked")).hasSize(1);
        assertThat(findPermanent(player1, "Wicked").getId()).isNotEqualTo(firstRole.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore);
    }

    @Test
    void roleGoingToGraveyardAfterEnchantedCreatureDiesDrainsOpponentOnce() {
        Permanent target = addCreatureReady(player2, new BesottedKnight());
        castTwistedFealty(List.of(target.getId(), target.getId()));
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int ownLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wicked")).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore);
    }

    private void castTwistedFealty(List<UUID> targets) {
        prepareTwistedFealty();
        harness.castAndResolveSorcery(player1, 0, targets);
    }

    private void prepareTwistedFealty() {
        harness.setHand(player1, List.of(new TwistedFealty()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
