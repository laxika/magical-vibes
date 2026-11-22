package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.d.DanceOfMany;
import com.github.laxika.magicalvibes.cards.e.ExaltedAngel;
import com.github.laxika.magicalvibes.cards.k.KayaIntangibleSlayer;
import com.github.laxika.magicalvibes.cards.s.SecondHarvest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SplinterTwin;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A copy of a face-down permanent copies only the characteristics its face-down status lists (CR 707.2, 708.2): a
 * face-up, nameless 2/2 creature with no mana cost and no abilities, with ward {2} when it was disguised or cloaked
 * (CR 702.168a, 701.58a), and nothing of the hidden card reaches a player.
 */
@CardUsed({Clone.class, DanceOfMany.class, ExaltedAngel.class, KayaIntangibleSlayer.class, Shock.class,
        SplinterTwin.class, SecondHarvest.class})
class FaceDownTokenCopyTest extends BaseCardTest {

    private static final String HIDDEN_NAME = "Exalted Angel";

    @Test
    @DisplayName("Dance of Many's token copy of a face-down creature is a face-up nameless 2/2 with no abilities")
    void danceOfManyCopiesFaceDownCharacteristics() {
        Permanent hidden = faceDownAngel(player2);
        harness.clearMessages();

        castDanceOfMany(hidden);

        Permanent token = token(player1);
        assertFaceDownValues(token);
        assertThat(gqs.hasKeyword(gd, token, Keyword.WARD)).isFalse();
        assertHiddenFrom(player1);
    }

    @Test
    @DisplayName("A token copy of a disguised creature has ward {2}, which triggers when an opponent targets it")
    void tokenCopyOfDisguisedCreatureHasWard() {
        Permanent hidden = addCreatureReady(player1, new ExaltedAngel());
        hidden.setFaceDownAsDisguised();
        harness.clearMessages();

        castDanceOfMany(hidden);

        Permanent token = token(player1);
        assertFaceDownValues(token);
        assertThat(gqs.hasKeyword(gd, token, Keyword.WARD)).isTrue();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, token.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertHiddenFrom(player2);
    }

    @Test
    @DisplayName("Splinter Twin on a face-down creature creates a nameless 2/2 token with haste")
    void splinterTwinCopiesFaceDownCharacteristics() {
        Permanent hidden = faceDownAngel(player1);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SplinterTwin());
        aura.setAttachedTo(hidden.getId());
        harness.clearMessages();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = token(player1);
        assertFaceDownValues(token);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertHiddenFrom(player2);
    }

    @Test
    @DisplayName("Splinter Twin's copy of a face-down creature that left in response uses its face-down values")
    void splinterTwinCopiesLastKnownFaceDownCharacteristics() {
        Permanent hidden = faceDownAngel(player1);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SplinterTwin());
        aura.setAttachedTo(hidden.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, hidden));

        harness.passBothPriorities();

        Permanent token = token(player1);
        assertFaceDownValues(token);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Kaya's token copy of an exiled face-down creature copies it as it last existed, face down")
    void kayaCopiesExiledFaceDownCreatureAsItLastExisted() {
        Permanent kaya = harness.addToBattlefieldAndReturn(player1, new KayaIntangibleSlayer());
        kaya.setCounterCount(CounterType.LOYALTY, 4);
        kaya.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent hidden = faceDownAngel(player2);

        harness.activateAbility(player1, 0, 2, null, hidden.getId());
        harness.passBothPriorities();

        Permanent token = token(player1);
        assertThat(token.getCard().getName()).isEmpty();
        assertThat(token.getCard().getSetCode()).isNull();
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING).doesNotContain(Keyword.LIFELINK);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Clone entering as a copy of a disguised creature is a face-up nameless 2/2 with ward")
    void cloneOfDisguisedCreatureHasWard() {
        Permanent hidden = addCreatureReady(player2, new ExaltedAngel());
        hidden.setFaceDownAsDisguised();
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, hidden.getId());

        Permanent clone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElseThrow();
        assertThat(clone.isFaceDown()).isFalse();
        assertThat(clone.getCard().getName()).isEmpty();
        assertThat(gqs.getEffectivePower(gd, clone)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, clone, Keyword.WARD)).isTrue();
        assertThat(gqs.hasKeyword(gd, clone, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The creation log describes a token copy of a face-down permanent, with no blank name")
    void creationLogDescribesCopyOfFaceDownPermanent() {
        Permanent hidden = faceDownAngel(player2);

        castDanceOfMany(hidden);

        assertThat(logLines()).anyMatch(line -> line.equals("A token copy of a face-down permanent is created."))
                .noneMatch(line -> line.contains("  ") || line.contains("of  "));
    }

    @Test
    @DisplayName("Later text calls the nameless token a nameless token, and it still has no name")
    void laterTextCallsTheTokenNameless() {
        Permanent hidden = faceDownAngel(player2);
        castDanceOfMany(hidden);
        Permanent token = token(player1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, token.getId());
        harness.passBothPriorities();

        assertThat(token.getCard().getName()).isEmpty();
        assertThat(token.getPublicName()).isEqualTo("a nameless token");
        assertThat(logLines()).anyMatch(line -> line.contains("a nameless token"))
                .noneMatch(line -> line.contains("  "));
    }

    @Test
    @DisplayName("Clone of a face-down creature is called a nameless permanent in text")
    void cloneOfFaceDownCreatureIsCalledNameless() {
        Permanent hidden = faceDownAngel(player2);
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, hidden.getId());
        Permanent clone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElseThrow();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, clone.getId());
        harness.passBothPriorities();

        assertThat(clone.getPublicName()).isEqualTo("a nameless permanent");
        assertThat(logLines()).anyMatch(line -> line.contains("a nameless permanent"))
                .noneMatch(line -> line.contains("  "));
    }

    @Test
    @DisplayName("Second Harvest's copy of a face-down token is a face-up nameless 2/2 with no abilities")
    void secondHarvestCopiesAFaceDownTokensFaceDownValues() {
        ExaltedAngel tokenAngel = new ExaltedAngel();
        tokenAngel.setToken(true);
        Permanent hidden = addCreatureReady(player1, tokenAngel);
        hidden.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new SecondHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken() && !permanent.getId().equals(hidden.getId()))
                .findFirst().orElseThrow();
        assertFaceDownValues(copy);
    }

    private List<String> logLines() {
        return gd.gameLog.stream().map(GameLogEntry::plainText).toList();
    }

    private Permanent faceDownAngel(Player controller) {
        Permanent permanent = addCreatureReady(controller, new ExaltedAngel());
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        return permanent;
    }

    private void castDanceOfMany(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DanceOfMany()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();
    }

    private Permanent token(Player controller) {
        return gd.playerBattlefields.get(controller.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
    }

    private void assertFaceDownValues(Permanent token) {
        assertThat(token.isFaceDown()).isFalse();
        assertThat(token.getCard().getName()).isEmpty();
        assertThat(token.getCard().getSetCode()).isNull();
        assertThat(token.getCard().getCollectorNumber()).isNull();
        assertThat(token.getCard().getManaCost()).isEmpty();
        assertThat(token.getCard().getSubtypes()).isEmpty();
        assertThat(token.getCard().getKeywords()).doesNotContain(Keyword.FLYING, Keyword.LIFELINK, Keyword.MORPH);
        assertThat(token.getCard().getActivatedAbilities(com.github.laxika.magicalvibes.model.Zone.BATTLEFIELD))
                .isEmpty();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, token)).isEmpty();
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isFalse();
    }

    /** Neither the log nor what was sent to {@code opponent}, who may not look at the face-down card, names it. */
    private void assertHiddenFrom(Player opponent) {
        harness.publishState();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(line -> line.contains(HIDDEN_NAME));
        var connection = opponent == player1 ? harness.getConn1() : harness.getConn2();
        assertThat(connection.getSentMessages()).isNotEmpty()
                .allSatisfy(message -> assertThat(message).doesNotContain(HIDDEN_NAME));
    }
}
