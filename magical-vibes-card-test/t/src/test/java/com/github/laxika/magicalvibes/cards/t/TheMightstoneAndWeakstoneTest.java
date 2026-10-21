package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RustGoliath;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheMightstoneAndWeakstone.class, ArgothianSprite.class, EnergyRefractor.class,
        Forest.class, RustGoliath.class})
class TheMightstoneAndWeakstoneTest extends BaseCardTest {

    @Test
    @DisplayName("The Mightstone and Weakstone's draw mode draws two cards")
    void drawModeDrawsTwoCards() {
        harness.setLibrary(player1, List.of(new ArgothianSprite(), new ArgothianSprite()));
        cast(0, null);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The Mightstone and Weakstone's debuff mode gives a creature -5/-5")
    void debuffModeGivesTargetCreatureMinusFiveMinusFive() {
        Permanent target = addCreatureReady(player1, new RustGoliath());
        castDebuff(target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("The debuff mode rejects a noncreature target")
    void debuffModeRejectsNoncreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new TheMightstoneAndWeakstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, land.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The tap ability produces two Powerstone-restricted colorless mana")
    void tapAbilityProducesPowerstoneMana() {
        Permanent mightstone = harness.addToBattlefieldAndReturn(player1, new TheMightstoneAndWeakstone());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(2);
        assertThat(mightstone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Powerstone mana can cast artifacts")
    void powerstoneManaCastsArtifacts() {
        harness.addToBattlefield(player1, new TheMightstoneAndWeakstone());
        harness.activateAbility(player1, 0, null, null);

        harness.setHand(player1, List.of(new EnergyRefractor()));
        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
    }

    @Test
    @DisplayName("Powerstone mana cannot pay the generic cost of nonartifact spells")
    void powerstoneManaRestrictsNonartifactSpells() {
        harness.addToBattlefield(player1, new TheMightstoneAndWeakstone());
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new ArgothianSprite()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void powerstoneManaPaysForNonartifactActivatedAbilities() {
        harness.addToBattlefield(player1, new TheMightstoneAndWeakstone());
        Permanent sprite = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThat(sprite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
    }

    @Test
    void debuffKillsOpponentsCreature() {
        harness.addToBattlefield(player2, new ArgothianSprite());
        castDebuff(findPermanent(player2, "Argothian Sprite").getId());

        harness.assertNotOnBattlefield(player2, "Argothian Sprite");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof ArgothianSprite);
    }

    @Test
    void debuffExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RustGoliath());
        castDebuff(target.getId());
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(10);
    }

    @Test
    void cannotActivateManaAbilityAgainWhileTapped() {
        harness.addToBattlefield(player1, new TheMightstoneAndWeakstone());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(2);
    }

    /**
     * The debuff mode's target belongs to the enters-the-battlefield trigger, so it is chosen when that
     * trigger goes on the stack (CR 603.3d), not while the permanent spell is cast (CR 601.2c).
     */
    private void castDebuff(java.util.UUID targetId) {
        cast(1, null);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, targetId);
        resolveAllTriggers();
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new TheMightstoneAndWeakstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        gs.playCard(gd, player1, 0, mode, targetId, null);
    }
}
