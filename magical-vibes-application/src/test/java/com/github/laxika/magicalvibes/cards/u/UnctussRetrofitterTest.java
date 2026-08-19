package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnctussRetrofitterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes an artifact you control a 4/4 artifact creature")
    void animatesTargetArtifact() {
        FountainOfYouth fountain = new FountainOfYouth();
        harness.addToBattlefield(player1, fountain);
        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        castRetrofitter(List.of(fountainId));

        GameData gd = harness.getGameData();
        Permanent target = gqs.findPermanentById(gd, fountainId);

        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    @Test
    @DisplayName("The animation ends when Unctus's Retrofitter leaves the battlefield")
    void animationEndsWhenSourceLeaves() {
        FountainOfYouth fountain = new FountainOfYouth();
        harness.addToBattlefield(player1, fountain);
        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        castRetrofitter(List.of(fountainId));

        GameData gd = harness.getGameData();
        Permanent source = findPermanent(player1, "Unctus's Retrofitter");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));

        Permanent target = gqs.findPermanentById(gd, fountainId);
        assertThat(gqs.isCreature(gd, target)).isFalse();
    }

    @Test
    @DisplayName("The ETB ability can choose no artifact")
    void canChooseNoArtifact() {
        harness.setHand(player1, List.of(new UnctussRetrofitter()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Unctus's Retrofitter");
    }

    @Test
    @DisplayName("The ETB ability cannot target an artifact an opponent controls")
    void cannotTargetOpponentsArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.setHand(player1, List.of(new UnctussRetrofitter()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact you control");
    }

    private void castRetrofitter(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new UnctussRetrofitter()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
