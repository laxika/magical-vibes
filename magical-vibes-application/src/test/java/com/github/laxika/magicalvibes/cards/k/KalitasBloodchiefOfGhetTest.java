package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KalitasBloodchiefOfGhetTest extends BaseCardTest {

    private void activate(Permanent kalitas, UUID targetId) {
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int kalitasIndex = gd.playerBattlefields.get(player1.getId()).indexOf(kalitas);
        harness.activateAbility(player1, kalitasIndex, null, targetId);
        harness.passBothPriorities();
    }

    private long vampireTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Vampire"))
                .count();
    }

    @Test
    @DisplayName("Destroys a creature and creates a Vampire token with its last-known power and toughness")
    void destroysCreatureAndCreatesSizedVampire() {
        Permanent kalitas = addCreatureReady(player1, new KalitasBloodchiefOfGhet());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setPowerModifier(3);
        target.setToughnessModifier(1);

        activate(kalitas, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(vampireTokenCount(player1)).isEqualTo(1);
        assertThat(vampireTokenCount(player2)).isZero();

        Permanent vampire = findPermanent(player1, "Vampire");
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(3);
        assertThat(vampire.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(vampire.getCard().getSubtypes()).contains(CardSubtype.VAMPIRE);
    }

    @Test
    @DisplayName("Does not create a token when regeneration prevents the creature from dying")
    void doesNotCreateTokenWhenTargetRegenerates() {
        Permanent kalitas = addCreatureReady(player1, new KalitasBloodchiefOfGhet());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setRegenerationShield(1);

        activate(kalitas, target.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(vampireTokenCount(player1)).isZero();
    }

    @Test
    @DisplayName("Can target creatures but not noncreature permanents")
    void cannotTargetNoncreaturePermanent() {
        Permanent kalitas = addCreatureReady(player1, new KalitasBloodchiefOfGhet());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID artifactId = harness.getPermanentId(player2, "Fountain of Youth");

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int kalitasIndex = gd.playerBattlefields.get(player1.getId()).indexOf(kalitas);

        assertThatThrownBy(() -> harness.activateAbility(player1, kalitasIndex, null, artifactId))
                .isInstanceOf(IllegalStateException.class);
    }
}
