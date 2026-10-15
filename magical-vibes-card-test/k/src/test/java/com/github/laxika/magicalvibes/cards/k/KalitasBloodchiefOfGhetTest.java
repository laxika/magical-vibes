package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.EldraziMonument;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KalitasBloodchiefOfGhet.class, GrizzlyBears.class, FountainOfYouth.class,
        LeylineOfTheVoid.class, EldraziMonument.class})
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

    @Test
    @DisplayName("Does not create a Vampire when the destroyed creature is exiled instead of dying")
    void doesNotCreateTokenWhenDeathIsReplacedByExile() {
        Permanent kalitas = addCreatureReady(player1, new KalitasBloodchiefOfGhet());
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        activate(kalitas, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(vampireTokenCount(player1)).isZero();
    }

    @Test
    @DisplayName("Can destroy itself and still create a Vampire for its controller")
    void createsTokenWhenKalitasDestroysItself() {
        Permanent kalitas = addCreatureReady(player1, new KalitasBloodchiefOfGhet());

        activate(kalitas, kalitas.getId());

        harness.assertInGraveyard(player1, "Kalitas, Bloodchief of Ghet");
        harness.assertNotOnBattlefield(player1, "Kalitas, Bloodchief of Ghet");
        assertThat(vampireTokenCount(player1)).isEqualTo(1);
        Permanent vampire = findPermanent(player1, "Vampire");
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(5);
    }

    @Test
    @DisplayName("A creature token dying to the ability also creates a Vampire")
    void createsTokenWhenTargetIsAToken() {
        Permanent kalitas = addCreatureReady(player1, new KalitasBloodchiefOfGhet());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        activate(kalitas, target.getId());
        Permanent firstVampire = findPermanent(player1, "Vampire");
        kalitas.untap();

        activate(kalitas, firstVampire.getId());

        assertThat(gqs.findPermanentById(gd, firstVampire.getId())).isNull();
        assertThat(vampireTokenCount(player1)).isEqualTo(1);
        Permanent vampire = findPermanent(player1, "Vampire");
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating the ability taps Kalitas and spends three black mana")
    void paysManaAndTapCosts() {
        Permanent kalitas = addCreatureReady(player1, new KalitasBloodchiefOfGhet());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        activate(kalitas, target.getId());

        assertThat(kalitas.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.addMana(player1, ManaColor.BLACK, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, kalitas.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not create a Vampire when indestructible prevents destruction")
    void doesNotCreateTokenWhenTargetIsIndestructible() {
        Permanent kalitas = addCreatureReady(player1, new KalitasBloodchiefOfGhet());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new EldraziMonument());

        activate(kalitas, target.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(vampireTokenCount(player1)).isZero();
    }
}
