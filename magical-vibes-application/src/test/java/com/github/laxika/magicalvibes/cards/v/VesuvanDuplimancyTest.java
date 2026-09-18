package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BashToBits;
import com.github.laxika.magicalvibes.cards.d.DualShot;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VesuvanDuplimancy.class, GiantGrowth.class, IsamaruHoundOfKonda.class,
        BashToBits.class, Spellbook.class, DualShot.class, GrizzlyBears.class})
class VesuvanDuplimancyTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a single targeted creature without its legendary supertype")
    void createsNonlegendaryTokenCopyOfTargetedCreature() {
        harness.addToBattlefield(player1, new VesuvanDuplimancy());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = tokenCopies(player1);
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getSupertypes())
                .doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Copies a single targeted artifact")
    void createsTokenCopyOfTargetedArtifact() {
        harness.addToBattlefield(player1, new VesuvanDuplimancy());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new BashToBits()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).hasSize(1);
        assertThat(tokenCopies(player1).getFirst().getCard().getType())
                .isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Does not trigger for a spell with multiple targets")
    void doesNotTriggerForMultipleTargets() {
        harness.addToBattlefield(player1, new VesuvanDuplimancy());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(firstTarget.getId(), secondTarget.getId()));
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).isEmpty();
        assertThat(firstTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(secondTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for an artifact controlled by an opponent")
    void doesNotTriggerForOpponentsArtifact() {
        harness.addToBattlefield(player1, new VesuvanDuplimancy());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new BashToBits()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).isEmpty();
    }

    private List<Permanent> tokenCopies(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
