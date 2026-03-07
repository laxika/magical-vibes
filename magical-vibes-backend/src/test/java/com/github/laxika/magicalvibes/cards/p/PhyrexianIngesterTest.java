package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfByImprintedCreaturePTEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndImprintEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianIngesterTest extends BaseCardTest {

    private void castIngesterAndAcceptMay(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PhyrexianIngester()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → target selection
        harness.handlePermanentChosen(player1, targetId); // choose target → ETB on stack
        harness.passBothPriorities(); // resolve ETB → exile + imprint
    }

    // ===== Card structure =====

    @Test
    @DisplayName("Card has MayEffect wrapping ExileTargetPermanentAndImprintEffect on ETB and static boost")
    void hasCorrectEffects() {
        PhyrexianIngester card = new PhyrexianIngester();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(ExileTargetPermanentAndImprintEffect.class);

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(BoostSelfByImprintedCreaturePTEffect.class);
    }

    // ===== ETB exile =====

    @Test
    @DisplayName("ETB exiles target nontoken creature and imprints it")
    void etbExilesTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castIngesterAndAcceptMay(bearsId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== P/T boost from imprinted creature =====

    @Test
    @DisplayName("Gets +X/+Y equal to exiled creature's power and toughness")
    void boostsFromImprintedCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castIngesterAndAcceptMay(bearsId);

        Permanent ingester = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Ingester"))
                .findFirst().orElseThrow();

        // Base 3/3 + Grizzly Bears 2/2 = 5/5
        assertThat(gqs.getEffectivePower(gd, ingester)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ingester)).isEqualTo(5);
    }

    @Test
    @DisplayName("Gets correct boost with asymmetric P/T creature")
    void boostsFromAsymmetricCreature() {
        harness.addToBattlefield(player2, new PhyrexianDigester()); // 2/1
        UUID digesterId = harness.getPermanentId(player2, "Phyrexian Digester");

        castIngesterAndAcceptMay(digesterId);

        Permanent ingester = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Ingester"))
                .findFirst().orElseThrow();

        // Base 3/3 + Phyrexian Digester 2/1 = 5/4
        assertThat(gqs.getEffectivePower(gd, ingester)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ingester)).isEqualTo(4);
    }

    // ===== No boost without imprint =====

    @Test
    @DisplayName("No P/T boost when may ability is declined")
    void noBoostWhenDeclined() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PhyrexianIngester()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve → may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        Permanent ingester = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Ingester"))
                .findFirst().orElseThrow();

        // Base 3/3, no boost
        assertThat(gqs.getEffectivePower(gd, ingester)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ingester)).isEqualTo(3);

        // Grizzly Bears still on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Can exile own creature =====

    @Test
    @DisplayName("Can exile own creature")
    void canExileOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        castIngesterAndAcceptMay(bearsId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        Permanent ingester = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Ingester"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, ingester)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ingester)).isEqualTo(5);
    }

    // ===== Exile is permanent =====

    @Test
    @DisplayName("Exiled creature stays exiled when Phyrexian Ingester leaves the battlefield")
    void exiledCreatureStaysExiledWhenIngesterLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castIngesterAndAcceptMay(bearsId);

        // No O-ring style tracking
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }
}
