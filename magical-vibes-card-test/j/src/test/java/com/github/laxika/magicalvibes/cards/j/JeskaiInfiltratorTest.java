package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.ArashinCleric;
import com.github.laxika.magicalvibes.cards.s.SupplantForm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JeskaiInfiltrator.class, ArashinCleric.class, SupplantForm.class})
class JeskaiInfiltratorTest extends BaseCardTest {

    @Test
    void cannotBeBlockedWhileItIsYourOnlyCreature() {
        Permanent infiltrator = addReadyInfiltrator();
        infiltrator.setAttacking(true);
        addReadyBlocker();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void canBeBlockedWhenYouControlAnotherCreature() {
        Permanent infiltrator = addReadyInfiltrator();
        infiltrator.setAttacking(true);
        addReadyInfiltrator();
        addReadyBlocker();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    void combatDamageExilesAndManifestsSourceAndTopCard() {
        Permanent infiltrator = addReadyInfiltrator();
        infiltrator.setAttacking(true);
        Card topCard = new ArashinCleric();
        harness.setLibrary(player1, List.of(topCard));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2);
        assertThat(battlefield).allMatch(permanent -> permanent.isFaceDown() && permanent.isManifested());
        assertThat(battlefield).anyMatch(permanent -> permanent.getCard().getId().equals(infiltrator.getCard().getId()));
        assertThat(battlefield).anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryStillManifestsInfiltrator() {
        Permanent infiltrator = addReadyInfiltrator();
        harness.setLibrary(player1, List.of());

        dealCombatDamage(infiltrator);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(infiltrator.getCard().getId());
                    assertThat(permanent.isFaceDown()).isTrue();
                    assertThat(permanent.isManifested()).isTrue();
                    assertThat(permanent.getId()).isNotEqualTo(infiltrator.getId());
                    assertThat(permanent.isAttacking()).isFalse();
                    assertThat(permanent.isSummoningSick()).isTrue();
                });
    }

    @Test
    void sourceRemovedInResponseStillManifestsTopCard() {
        Permanent infiltrator = addReadyInfiltrator();
        Card topCard = new ArashinCleric();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player2, List.of(new SupplantForm()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        dealCombatDamage(infiltrator);
        harness.castInstant(player2, 0, infiltrator.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Jeskai Infiltrator");
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(topCard.getId());
                    assertThat(permanent.isFaceDown()).isTrue();
                    assertThat(permanent.isManifested()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void manifestDoesNotTriggerPrintedEnterAbilityOrTurningFaceUp() {
        Permanent infiltrator = addReadyInfiltrator();
        Card topCard = new ArashinCleric();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);

        dealCombatDamage(infiltrator);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        int clericIndex = battlefield.getFirst().getCard().getId().equals(topCard.getId()) ? 0 : 1;
        Permanent manifestedCleric = battlefield.get(clericIndex);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, clericIndex);
        resolveAllTriggers();

        assertThat(manifestedCleric.isFaceDown()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    void tokenCopyCannotReturnAfterExilingItself() {
        Permanent original = addCreatureReady(player2, new JeskaiInfiltrator());
        harness.setHand(player1, List.of(new SupplantForm()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, original.getId());
        resolveAllTriggers();
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        token.setSummoningSick(false);
        Card topCard = new ArashinCleric();
        harness.setLibrary(player1, List.of(topCard));

        dealCombatDamage(token);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(topCard.getId());
                    assertThat(permanent.isFaceDown()).isTrue();
                    assertThat(permanent.isManifested()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void noncreatureCardIsManifestedButCannotTurnFaceUpForManaCost() {
        Permanent infiltrator = addReadyInfiltrator();
        Card topCard = new SupplantForm();
        harness.setLibrary(player1, List.of(topCard));

        dealCombatDamage(infiltrator);
        resolveAllTriggers();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2);
        int spellIndex = battlefield.getFirst().getCard().getId().equals(topCard.getId()) ? 0 : 1;
        Permanent manifestedSpell = battlefield.get(spellIndex);
        assertThat(manifestedSpell.isFaceDown()).isTrue();
        assertThat(manifestedSpell.isManifested()).isTrue();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, spellIndex))
                .isInstanceOf(IllegalStateException.class);
        assertThat(manifestedSpell.isFaceDown()).isTrue();
    }

    @Test
    void faceDownInfiltratorDoesNotRetainItsUnblockableAbility() {
        Permanent infiltrator = addReadyInfiltrator();
        harness.setLibrary(player1, List.of());
        dealCombatDamage(infiltrator);
        resolveAllTriggers();
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        manifested.setSummoningSick(false);
        manifested.setAttacking(true);
        addReadyBlocker();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isBlocking()).isTrue();
    }

    private void dealCombatDamage(Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReadyInfiltrator() {
        return addCreatureReady(player1, new JeskaiInfiltrator());
    }

    private Permanent addReadyBlocker() {
        return addCreatureReady(player2, new ArashinCleric());
    }
}
