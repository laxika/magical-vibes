package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BloodtitheHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a Blood token")
    void etbCreatesBloodToken() {
        harness.setHand(player1, List.of(new BloodtitheHarvester()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bloodTokenCount(player1)).isEqualTo(1);
        Permanent blood = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blood"))
                .findFirst().orElseThrow();
        assertThat(blood.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(blood.getCard().getSubtypes()).contains(CardSubtype.BLOOD);
        assertThat(blood.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Sac ability gives target -X/-X where X is twice Blood tokens controlled")
    void sacGivesMinusTwiceBloodCount() {
        Permanent harvester = addReadyHarvester();
        addBloodToken(player1);
        addBloodToken(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        forceMainPhase(player1);

        // 2 Blood ⇒ X = 4 ⇒ -4/-4
        harness.activateAbility(player1, indexOf(player1, harvester), null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bloodtithe Harvester");
        harness.assertInGraveyard(player1, "Bloodtithe Harvester");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("With one Blood token, target gets -2/-2")
    void oneBloodGivesMinusTwo() {
        Permanent harvester = addReadyHarvester();
        addBloodToken(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        forceMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, harvester), null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("With zero Blood tokens, target gets -0/-0")
    void zeroBloodIsNoDebuff() {
        Permanent harvester = addReadyHarvester();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        forceMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, harvester), null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Debuff wears off at cleanup")
    void debuffWearsOff() {
        Permanent harvester = addReadyHarvester();
        addBloodToken(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        forceMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, harvester), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate at instant speed")
    void cannotActivateAtInstantSpeed() {
        Permanent harvester = addReadyHarvester();
        addBloodToken(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        UUID targetId = target.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, harvester), null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent harvester = addReadyHarvester();
        addBloodToken(player1);
        Permanent blood = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blood"))
                .findFirst().orElseThrow();
        forceMainPhase(player1);

        UUID bloodId = blood.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, harvester), null, bloodId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyHarvester() {
        Permanent perm = new Permanent(new BloodtitheHarvester());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void forceMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private long bloodTokenCount(Player owner) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blood"))
                .count();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void addBloodToken(Player player) {
        Card bloodCard = new Card();
        bloodCard.setName("Blood");
        bloodCard.setType(CardType.ARTIFACT);
        bloodCard.setManaCost("");
        bloodCard.setToken(true);
        bloodCard.setColor(null);
        bloodCard.setSubtypes(List.of(CardSubtype.BLOOD));
        bloodCard.addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new DiscardCardTypeCost(null, null), new SacrificeSelfCost(), new DrawCardEffect()),
                "{1}, {T}, Discard a card, Sacrifice this token: Draw a card."
        ));
        Permanent blood = new Permanent(bloodCard);
        blood.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(blood);
    }
}
