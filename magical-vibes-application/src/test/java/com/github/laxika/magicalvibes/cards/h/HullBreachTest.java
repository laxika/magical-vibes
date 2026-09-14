package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.cards.w.WarpedDevotion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HullBreach.class, ManaCylix.class, WarpedDevotion.class})
class HullBreachTest extends BaseCardTest {

    @Test
    void destroysTargetArtifact() {
        harness.addToBattlefield(player2, new ManaCylix());
        cast(0, harness.getPermanentId(player2, "Mana Cylix"));

        harness.assertNotOnBattlefield(player2, "Mana Cylix");
        harness.assertInGraveyard(player2, "Mana Cylix");
    }

    @Test
    void destroysTargetEnchantment() {
        harness.addToBattlefield(player2, new WarpedDevotion());
        cast(1, harness.getPermanentId(player2, "Warped Devotion"));

        harness.assertNotOnBattlefield(player2, "Warped Devotion");
        harness.assertInGraveyard(player2, "Warped Devotion");
    }

    @Test
    void destroysTargetArtifactAndEnchantment() {
        harness.addToBattlefield(player2, new ManaCylix());
        harness.addToBattlefield(player2, new WarpedDevotion());
        UUID artifactId = harness.getPermanentId(player2, "Mana Cylix");
        UUID enchantmentId = harness.getPermanentId(player2, "Warped Devotion");

        harness.setHand(player1, List.of(new HullBreach()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castModalSorcery(player1, 0, 2, List.of(artifactId, enchantmentId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mana Cylix");
        harness.assertNotOnBattlefield(player2, "Warped Devotion");
    }

    @Test
    void canUseTheSameArtifactEnchantmentForBothTargets() {
        Card card = new Card();
        card.setName("Test Relic");
        card.setType(CardType.ARTIFACT);
        card.setAdditionalTypes(Set.of(CardType.ENCHANTMENT));
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, card);

        harness.setHand(player1, List.of(new HullBreach()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castModalSorcery(player1, 0, 2, List.of(permanent.getId(), permanent.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Test Relic");
    }

    @Test
    void rejectsWrongTargetTypeForEachMode() {
        harness.addToBattlefield(player2, new ManaCylix());
        harness.addToBattlefield(player2, new WarpedDevotion());
        harness.setHand(player1, List.of(new HullBreach()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        UUID artifactId = harness.getPermanentId(player2, "Mana Cylix");
        UUID enchantmentId = harness.getPermanentId(player2, "Warped Devotion");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, enchantmentId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, artifactId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 2, List.of(artifactId, artifactId)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new HullBreach()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }
}
