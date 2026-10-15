package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.d.Decommission;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SleekSchooner;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AerialModification.class, FountainOfYouth.class, GrizzlyBears.class,
        SleekSchooner.class, Decommission.class})
class AerialModificationTest extends BaseCardTest {

    @Test
    @DisplayName("Aerial Modification makes a Vehicle a creature and gives it +2/+2 and flying")
    void modifiesVehicle() {
        Permanent schooner = harness.addToBattlefieldAndReturn(player1, new SleekSchooner());

        castAerialModification(schooner);

        assertThat(gqs.isCreature(gd, schooner)).isTrue();
        assertThat(gqs.getEffectivePower(gd, schooner)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, schooner)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, schooner, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Aerial Modification gives an enchanted creature +2/+2 and flying")
    void modifiesCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAerialModification(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Aerial Modification cannot target a noncreature non-Vehicle permanent")
    void cannotTargetOtherPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new AerialModification()));
        addAerialModificationMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    @Test
    @DisplayName("Aerial Modification can animate an opponent's Vehicle without changing its controller or artifact type")
    void modifiesOpponentsVehicle() {
        Permanent schooner = harness.addToBattlefieldAndReturn(player2, new SleekSchooner());

        castAerialModification(schooner);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(schooner);
        assertThat(gqs.isArtifact(gd, schooner)).isTrue();
        assertThat(gqs.isCreature(gd, schooner)).isTrue();
        assertThat(gqs.getEffectivePower(gd, schooner)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, schooner)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, schooner, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Destroying Aerial Modification ends animation and flying on an uncrewed Vehicle")
    void destroyingAuraEndsVehicleEffects() {
        Permanent schooner = harness.addToBattlefieldAndReturn(player1, new SleekSchooner());
        castAerialModification(schooner);
        assertThat(gqs.isCreature(gd, schooner)).isTrue();

        harness.setHand(player1, List.of(new Decommission()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Aerial Modification"));

        harness.assertInGraveyard(player1, "Aerial Modification");
        harness.assertOnBattlefield(player1, "Sleek Schooner");
        assertThat(gqs.isCreature(gd, schooner)).isFalse();
        assertThat(gqs.isArtifact(gd, schooner)).isTrue();
        assertThat(gqs.hasKeyword(gd, schooner, Keyword.FLYING)).isFalse();
    }

    private void castAerialModification(Permanent target) {
        harness.setHand(player1, List.of(new AerialModification()));
        addAerialModificationMana();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addAerialModificationMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
