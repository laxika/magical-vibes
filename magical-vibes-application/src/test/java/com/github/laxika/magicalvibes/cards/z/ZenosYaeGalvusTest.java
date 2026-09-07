package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShinryuTranscendentRival;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZenosYaeGalvus.class, ShinryuTranscendentRival.class, GrizzlyBears.class})
class ZenosYaeGalvusTest extends BaseCardTest {

    @Test
    void controllerChoosesOpposingCreatureAndDebuffsAllOthers() {
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        Permanent debuffed = addCreatureReady(player2, new GrizzlyBears());
        Permanent zenos = castZenos();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(chosen.getId(), debuffed.getId());

        harness.handlePermanentChosen(player1, chosen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zenos);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(chosen);
    }

    @Test
    void transformsWhenTheChosenCreatureLeavesTheBattlefield() {
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        Permanent zenos = castZenos();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, chosen));
        harness.passBothPriorities();

        assertThat(zenos.isTransformed()).isTrue();
        assertThat(zenos.getCard()).isInstanceOf(ShinryuTranscendentRival.class);
    }

    private Permanent castZenos() {
        harness.setHand(player1, List.of(new ZenosYaeGalvus()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof ZenosYaeGalvus)
                .findFirst()
                .orElseThrow();
    }
}
