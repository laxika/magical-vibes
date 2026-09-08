package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InexorableBlobTest extends BaseCardTest {

    @Test
    void createsTappedAndAttackingOozeWithDelirium() {
        Permanent blob = addReadyBlob();
        setDelirium();

        declareAttack(blob);
        harness.passBothPriorities();

        List<Permanent> oozes = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Ooze"))
                .toList();
        assertThat(oozes).hasSize(1);
        assertThat(oozes.getFirst().isTapped()).isTrue();
        assertThat(oozes.getFirst().isAttackedThisTurn()).isTrue();
        assertThat(oozes.getFirst().getCard().getPower()).isEqualTo(3);
        assertThat(oozes.getFirst().getCard().getToughness()).isEqualTo(3);
    }

    @Test
    void doesNotCreateOozeWithoutDelirium() {
        Permanent blob = addReadyBlob();
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new LeoninScimitar()));

        declareAttack(blob);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Ooze"));
    }

    @Test
    void rechecksDeliriumWhenAttackTriggerResolves() {
        Permanent blob = addReadyBlob();
        setDelirium();

        declareAttack(blob);
        gd.playerGraveyards.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Ooze"));
    }

    private Permanent addReadyBlob() {
        Permanent blob = new Permanent(new InexorableBlob());
        blob.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(blob);
        return blob;
    }

    private void declareAttack(Permanent blob) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(blob)));
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Shock(), new LeoninScimitar(), new Pacifism()));
    }
}
