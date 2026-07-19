package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EtherswornAdjudicatorTest extends BaseCardTest {

    // ===== {1}{W}{B}, {T}: Destroy target creature or enchantment. =====

    @Test
    @DisplayName("Destroy ability destroys target creature")
    void destroysTargetCreature() {
        addAdjudicatorReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        addDestroyMana(player1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Destroy ability destroys target enchantment")
    void destroysTargetEnchantment() {
        addAdjudicatorReady(player1);
        harness.addToBattlefield(player2, new AngelicChorus());
        addDestroyMana(player1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonenchantment permanent")
    void cannotTargetArtifact() {
        addAdjudicatorReady(player1);
        harness.addToBattlefield(player2, new FountainOfYouth());
        addDestroyMana(player1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== {2}{U}: Untap this creature. =====

    @Test
    @DisplayName("Untap ability untaps Ethersworn Adjudicator")
    void untapAbilityUntapsSelf() {
        Permanent adjudicator = addAdjudicatorReady(player1);
        adjudicator.tap();
        assertThat(adjudicator.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(adjudicator.isTapped()).isFalse();
    }

    // ===== Helpers =====

    private Permanent addAdjudicatorReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new EtherswornAdjudicator());
        perm.setSummoningSick(false);
        return perm;
    }

    private void addDestroyMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
