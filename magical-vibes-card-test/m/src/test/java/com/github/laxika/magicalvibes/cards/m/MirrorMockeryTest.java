package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.d.DeathmistRaptor;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.SarkhansRage;
import com.github.laxika.magicalvibes.cards.t.TreadUpon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorMockery.class, ColossodonYearling.class, DeathmistRaptor.class,
        Naturalize.class, SarkhansRage.class, TreadUpon.class})
class MirrorMockeryTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with the enchanted creature offers to create a normal token copy")
    void attackingOffersTokenCopy() {
        Permanent creature = addCreatureReady(player1, new ColossodonYearling());
        castMirrorMockery(creature);

        keepCombatOpen();
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent copy = findTokenCopy();
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.isAttackedThisTurn()).isFalse();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(copy.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT));
    }

    @Test
    @DisplayName("Declining the attack trigger does not create a token")
    void decliningDoesNotCreateToken() {
        Permanent creature = addCreatureReady(player1, new ColossodonYearling());
        castMirrorMockery(creature);

        keepCombatOpen();
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The token copy is exiled at end of combat")
    void tokenCopyIsExiledAtEndOfCombat() {
        Permanent creature = addCreatureReady(player1, new ColossodonYearling());
        castMirrorMockery(creature);

        keepCombatOpen();
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent copy = findTokenCopy();

        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    @Test
    @DisplayName("The Aura controller creates the copy when an opponent's enchanted creature attacks")
    void opponentAttackingCreatesCopyForAuraController() {
        Permanent creature = addCreatureReady(player2, new ColossodonYearling());
        castMirrorMockery(creature);

        keepCombatOpen();
        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(creature)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent copy = findTokenCopy();
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.isAttackedThisTurn()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Destroying the Aura in response does not stop its copy or the delayed exile")
    void auraLeavingDoesNotStopCopyOrExile() {
        Permanent creature = addCreatureReady(player1, new ColossodonYearling());
        castMirrorMockery(creature);
        Permanent aura = findPermanent(player1, "Mirror Mockery");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.castInstant(player2, 0, aura.getId());
        keepCombatOpen();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent copy = findTokenCopy();

        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
    }

    @Test
    @DisplayName("The attack trigger copies the creature's last known values if it dies in response")
    void creatureLeavingStillCreatesCopy() {
        Permanent creature = addCreatureReady(player1, new ColossodonYearling());
        castMirrorMockery(creature);
        harness.setHand(player2, List.of(new SarkhansRage()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.castInstant(player2, 0, creature.getId());
        keepCombatOpen();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent copy = findTokenCopy();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(4);
    }

    @Test
    @DisplayName("A token copy does not inherit temporary boosts or granted trample")
    void temporaryBoostIsNotCopied() {
        Permanent creature = addCreatureReady(player1, new ColossodonYearling());
        castMirrorMockery(creature);
        harness.setHand(player1, List.of(new TreadUpon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        keepCombatOpen();
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent copy = findTokenCopy();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, copy, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Copying a face-down creature creates a face-up 2/2 without its printed abilities")
    void faceDownCharacteristicsAreCopied() {
        harness.setHand(player1, List.of(new DeathmistRaptor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent creature = findPermanent(player1, "Deathmist Raptor");
        creature.setSummoningSick(false);
        castMirrorMockery(creature);

        keepCombatOpen();
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent copy = findTokenCopy();
        assertThat(copy.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, copy, Keyword.DEATHTOUCH)).isFalse();
    }

    private void castMirrorMockery(Permanent creature) {
        harness.setHand(player1, List.of(new MirrorMockery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }

    private void keepCombatOpen() {
        harness.setHand(player2, List.of(new TreadUpon()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
    }

    private Permanent findTokenCopy() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
