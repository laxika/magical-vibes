package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireOfOrthanc.class, Forest.class, GrizzlyBears.class, SolRing.class, SuntailHawk.class})
class FireOfOrthancTest extends BaseCardTest {

    @Test
    @DisplayName("Resolves against an artifact or land")
    void destroysTargetArtifactOrLand() {
        harness.addToBattlefield(player2, new SolRing());
        harness.setHand(player1, List.of(new FireOfOrthanc()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Sol Ring");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sol Ring");
        harness.assertInGraveyard(player2, "Sol Ring");
    }

    @Test
    @DisplayName("Creatures without flying can't block this turn, fliers are unaffected")
    void nonFliersCantBlock() {
        harness.addToBattlefield(player2, new Forest());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent oppBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent oppHawk = addCreatureReady(player2, new SuntailHawk());

        harness.setHand(player1, List.of(new FireOfOrthanc()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(bls.canBlockAttacker(gd, ownBears, oppBears,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, oppBears, ownBears,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, oppHawk, ownBears,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FireOfOrthanc()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

}
