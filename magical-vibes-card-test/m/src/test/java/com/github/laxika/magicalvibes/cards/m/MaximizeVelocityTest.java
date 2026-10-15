package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaximizeVelocity.class, GrizzlyBears.class, Plains.class, DarksteelRelic.class})
class MaximizeVelocityTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +1/+1 and haste until end of turn")
    void boostsAndGrantsHaste() {
        castOnBears();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The boost and haste wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        castOnBears();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Jump-start discards a card, applies the spell, and exiles it")
    void jumpStartDiscardsAppliesAndExiles() {
        MaximizeVelocity spell = new MaximizeVelocity();
        Plains discarded = new Plains();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castJumpStart(player1, 0, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        Permanent battlefieldBears = findPermanent(player1, "Grizzly Bears");
        assertThat(battlefieldBears.getEffectivePower()).isEqualTo(3);
        assertThat(battlefieldBears.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.setHand(player1, List.of(new MaximizeVelocity()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.addToBattlefield(player1, new DarksteelRelic());
        UUID targetId = harness.getPermanentId(player1, "Darksteel Relic");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting from hand puts the spell into the graveyard")
    void normalCastGoesToGraveyard() {
        MaximizeVelocity spell = new MaximizeVelocity();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
    }

    @Test
    @DisplayName("Can boost and grant haste to an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MaximizeVelocity()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Granted haste allows a summoning-sick creature to attack")
    void hasteAllowsSummoningSickCreatureToAttack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(true);
        harness.setHand(player1, List.of(new MaximizeVelocity()));
        harness.addMana(player1, ManaColor.RED, 1);
        assertThat(als.canAttack(gd, bears, player1.getId())).isFalse();

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        assertThat(als.canAttack(gd, bears, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Jump-start accepts a nonland discard and pays the red mana cost")
    void jumpStartCanDiscardNonland() {
        MaximizeVelocity spell = new MaximizeVelocity();
        MaximizeVelocity discarded = new MaximizeVelocity();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castJumpStart(player1, 0, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Jump-start still requires mana and does not discard on a rejected cast")
    void jumpStartRequiresMana() {
        MaximizeVelocity spell = new MaximizeVelocity();
        Plains discarded = new Plains();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Jump-start requires a card to discard")
    void jumpStartRequiresDiscard() {
        MaximizeVelocity spell = new MaximizeVelocity();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Jump-start does not allow casting this sorcery during combat")
    void jumpStartRespectsSorceryTiming() {
        MaximizeVelocity spell = new MaximizeVelocity();
        Plains discarded = new Plains();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void castOnBears() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MaximizeVelocity()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
    }

}
