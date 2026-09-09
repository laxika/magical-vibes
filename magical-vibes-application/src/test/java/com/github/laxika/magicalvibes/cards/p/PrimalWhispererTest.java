package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimalWhisperer.class, GrizzlyBears.class})
class PrimalWhispererTest extends BaseCardTest {

    @Test
    void getsTwoPlusTwoForEachFaceDownCreatureOnTheBattlefield() {
        Permanent whisperer = harness.addToBattlefieldAndReturn(player1, new PrimalWhisperer());
        Permanent ownFaceDownCreature = addFaceDownCreature(player1);
        Permanent opposingFaceDownCreature = addFaceDownCreature(player2);

        assertThat(gqs.getEffectivePower(gd, whisperer)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, whisperer)).isEqualTo(6);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ownFaceDownCreature));

        assertThat(gqs.getEffectivePower(gd, whisperer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, whisperer)).isEqualTo(4);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, opposingFaceDownCreature));

        assertThat(gqs.getEffectivePower(gd, whisperer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, whisperer)).isEqualTo(2);
    }

    @Test
    void doesNotCountFaceDownNoncreatures() {
        Permanent whisperer = harness.addToBattlefieldAndReturn(player1, new PrimalWhisperer());
        Permanent faceDownNoncreature = new Permanent(new GrizzlyBears());
        faceDownNoncreature.setSummoningSick(false);
        faceDownNoncreature.setFaceDown(2, 2, Set.of(CardType.ARTIFACT));
        gd.playerBattlefields.get(player2.getId()).add(faceDownNoncreature);

        assertThat(gqs.getEffectivePower(gd, whisperer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, whisperer)).isEqualTo(2);
    }

    private Permanent addFaceDownCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
