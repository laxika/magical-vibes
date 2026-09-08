package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HullBreachTest extends BaseCardTest {

    @Test
    void destroysTargetArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        cast(0, harness.getPermanentId(player2, "Fountain of Youth"));

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    void destroysTargetEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        cast(1, harness.getPermanentId(player2, "Angelic Chorus"));

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    void destroysTargetArtifactAndEnchantment() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID artifactId = harness.getPermanentId(player2, "Fountain of Youth");
        UUID enchantmentId = harness.getPermanentId(player2, "Angelic Chorus");

        harness.setHand(player1, List.of(new HullBreach()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castModalSorcery(player1, 0, 2, List.of(artifactId, enchantmentId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    void canUseTheSameArtifactEnchantmentForBothTargets() {
        Card card = new Card();
        card.setName("Test Relic");
        card.setType(CardType.ARTIFACT);
        card.setAdditionalTypes(Set.of(CardType.ENCHANTMENT));
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player2.getId()).add(permanent);

        harness.setHand(player1, List.of(new HullBreach()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castModalSorcery(player1, 0, 2, List.of(permanent.getId(), permanent.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Test Relic");
    }

    @Test
    void rejectsWrongTargetTypeForEachMode() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new HullBreach()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        UUID artifactId = harness.getPermanentId(player2, "Fountain of Youth");

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
