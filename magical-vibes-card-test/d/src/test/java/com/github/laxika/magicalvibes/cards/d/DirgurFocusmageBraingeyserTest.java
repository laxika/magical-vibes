package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Braingeyser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DirgurFocusmageBraingeyser.class, Braingeyser.class})
class DirgurFocusmageBraingeyserTest extends BaseCardTest {

    @Test
    void highManaValueInstantOrSorceryFromHandPreparesDirgur() {
        Permanent dirgur = addCreatureReady(player1, new DirgurFocusmageBraingeyser());
        Braingeyser braingeyser = new Braingeyser();
        harness.setHand(player1, List.of(braingeyser));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(dirgur.isPrepared()).isTrue();
        assertThat(dirgur.getPreparedSpellCardId()).isNotNull();
        assertThat(gd.findExiledCard(dirgur.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    void lowerManaValueInstantOrSorceryDoesNotPrepareDirgur() {
        Permanent dirgur = addCreatureReady(player1, new DirgurFocusmageBraingeyser());
        harness.setHand(player1, List.of(new Braingeyser()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        assertThat(dirgur.isPrepared()).isFalse();
        assertThat(dirgur.getPreparedSpellCardId()).isNull();
    }

    @Test
    void castingPreparedBraingeyserDrawsChosenXAndDoesNotPrepareAgain() {
        Permanent dirgur = addCreatureReady(player1, new DirgurFocusmageBraingeyser());
        harness.setHand(player1, List.of(new Braingeyser()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());
        resolveAllTriggers();
        UUID preparedSpellId = dirgur.getPreparedSpellCardId();
        assertThat(preparedSpellId).isNotNull();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new DirgurFocusmageBraingeyser(),
                new DirgurFocusmageBraingeyser(), new DirgurFocusmageBraingeyser()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.ensurePriority(player1);

        gs.playCardFromExile(gd, player1, preparedSpellId, 3, player2.getId());

        assertThat(dirgur.isPrepared()).isFalse();
        assertThat(dirgur.getPreparedSpellCardId()).isNull();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(dirgur.isPrepared()).isFalse();
        assertThat(gd.findExiledCard(preparedSpellId)).isNull();
    }

    @Test
    void preparingAgainDoesNotCreateAnotherPreparedSpell() {
        Permanent dirgur = addCreatureReady(player1, new DirgurFocusmageBraingeyser());
        harness.setHand(player1, List.of(new Braingeyser(), new Braingeyser()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());
        resolveAllTriggers();
        UUID preparedSpellId = dirgur.getPreparedSpellCardId();
        assertThat(preparedSpellId).isNotNull();
        int exileSize = gd.exiledCards.size();

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());
        resolveAllTriggers();

        assertThat(dirgur.getPreparedSpellCardId()).isEqualTo(preparedSpellId);
        assertThat(gd.exiledCards).hasSize(exileSize);
    }

    @Test
    void opponentsSpellDoesNotPrepareDirgurOrReceiveItsDiscount() {
        Permanent dirgur = addCreatureReady(player1, new DirgurFocusmageBraingeyser());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Braingeyser()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player2, 0, 3, player1.getId());
        resolveAllTriggers();

        assertThat(dirgur.isPrepared()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void discountDoesNotReduceColoredManaForZeroX() {
        addCreatureReady(player1, new DirgurFocusmageBraingeyser());
        harness.setHand(player1, List.of(new Braingeyser()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }
}
