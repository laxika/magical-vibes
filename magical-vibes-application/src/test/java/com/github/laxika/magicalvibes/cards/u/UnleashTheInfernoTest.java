package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AladdinsRing;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.k.KeeningStone;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnleashTheInferno.class, AladdinsRing.class, ChandraNalaar.class, DarksteelRelic.class,
        KeeningStone.class, RagingGoblin.class, WallOfStone.class})
class UnleashTheInfernoTest extends BaseCardTest {

    @Test
    @DisplayName("Deals excess damage and destroys an eligible artifact")
    void destroysArtifactForExcessDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent eligibleArtifact = harness.addToBattlefieldAndReturn(player2, new KeeningStone());
        Permanent ineligibleArtifact = harness.addToBattlefieldAndReturn(player2, new AladdinsRing());

        cast(creature);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ineligibleArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, eligibleArtifact.getId());

        harness.assertNotOnBattlefield(player2, "Raging Goblin");
        harness.assertNotOnBattlefield(player2, "Keening Stone");
        harness.assertOnBattlefield(player2, "Aladdin's Ring");
    }

    @Test
    @DisplayName("Does not create the reflexive ability without excess damage")
    void doesNotDestroyArtifactWithoutExcessDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WallOfStone());
        harness.addToBattlefield(player2, new DarksteelRelic());

        cast(creature);

        assertThat(creature.getMarkedDamage()).isEqualTo(7);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Darksteel Relic");
    }

    @Test
    @DisplayName("Counts excess damage over a planeswalker's remaining loyalty")
    void countsPlaneswalkerExcessDamage() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);
        Permanent eligibleArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        Permanent ineligibleArtifact = harness.addToBattlefieldAndReturn(player2, new KeeningStone());

        cast(planeswalker);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ineligibleArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, eligibleArtifact.getId());

        harness.assertNotOnBattlefield(player2, "Chandra Nalaar");
        harness.assertNotOnBattlefield(player2, "Darksteel Relic");
        harness.assertOnBattlefield(player2, "Keening Stone");
    }

    @Test
    @DisplayName("Only accepts a creature or planeswalker as the damage target")
    void rejectsOtherDamageTargets() {
        harness.setHand(player1, List.of(new UnleashTheInferno()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new UnleashTheInferno()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}
