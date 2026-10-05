package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lictor.class, Forest.class})
class LictorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a trampling Tyranid Warrior when an opponent's creature entered this turn")
    void createsTokenAfterOpponentCreatureEnters() {
        harness.enterBattlefieldAndReturn(player2, new Lictor());

        castAndResolveLictor();

        Permanent token = findPermanent(player1, "Tyranid Warrior");
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.TYRANID, CardSubtype.WARRIOR);
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not create a token when only your creature entered this turn")
    void doesNotCreateTokenAfterOwnCreatureEnters() {
        harness.enterBattlefieldAndReturn(player1, new Lictor());

        castAndResolveLictor();

        assertThat(countPermanents(player1, "Tyranid Warrior")).isZero();
    }

    @Test
    @DisplayName("Does not create a token when only an opponent's noncreature permanent entered")
    void doesNotCreateTokenAfterOpponentNoncreatureEnters() {
        harness.enterBattlefieldAndReturn(player2, new Forest());

        castAndResolveLictor();

        assertThat(countPermanents(player1, "Tyranid Warrior")).isZero();
    }

    private void castAndResolveLictor() {
        harness.castFromHand(player1, new Lictor(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void doesNotTriggerWithoutAnOpponentCreatureEntry() {
        harness.castFromHand(player1, new Lictor(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Tyranid Warrior")).isZero();
    }

    @Test
    void preExistingOpponentCreatureDoesNotQualify() {
        harness.addToBattlefield(player2, new Lictor());

        castAndResolveLictor();

        assertThat(countPermanents(player1, "Tyranid Warrior")).isZero();
    }

    @Test
    void opponentCreatureEnteringAfterLictorCannotEnableItsAbility() {
        harness.castFromHand(player1, new Lictor(), "{3}{G}");
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player2, new Lictor());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Tyranid Warrior")).isZero();
    }

    @Test
    void createsOnlyOneTokenAfterMultipleOpponentCreatureEntries() {
        harness.enterBattlefieldAndReturn(player2, new Lictor());
        harness.enterBattlefieldAndReturn(player2, new Lictor());
        harness.passBothPriorities();
        harness.passBothPriorities();

        castAndResolveLictor();

        assertThat(countPermanents(player1, "Tyranid Warrior")).isEqualTo(1);
    }

    @Test
    void opponentCreatureStillQualifiesAfterItDies() {
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new Lictor());
        creature.setMarkedDamage(3);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);

        castAndResolveLictor();

        assertThat(countPermanents(player1, "Tyranid Warrior")).isEqualTo(1);
    }
}
