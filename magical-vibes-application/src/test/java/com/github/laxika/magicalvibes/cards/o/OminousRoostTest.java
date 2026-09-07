package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OminousRoost.class, Firebolt.class, GrizzlyBears.class, AirElemental.class})
class OminousRoostTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 blue flying Bird token")
    void enteringBattlefieldCreatesBird() {
        castRoost();

        Permanent bird = findPermanent(player1, "Bird");
        assertThat(bird.getEffectivePower()).isEqualTo(1);
        assertThat(bird.getEffectiveToughness()).isEqualTo(1);
        assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Casting a spell from your graveyard creates a Bird token")
    void castingFromGraveyardCreatesBird() {
        harness.addToBattlefield(player1, new OminousRoost());
        harness.setGraveyard(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell from hand does not create a Bird token")
    void castingFromHandDoesNotCreateBird() {
        harness.addToBattlefield(player1, new OminousRoost());
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird")).isZero();
    }

    @Test
    @DisplayName("A Bird token cannot block a creature without flying")
    void birdTokenCannotBlockGroundCreature() {
        Permanent bird = createBird();

        Permanent groundAttacker = addAttacker(new GrizzlyBears());
        prepareBlockers();
        final int birdIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bird);
        final int groundAttackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(groundAttacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(birdIndex, groundAttackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Bird token can block a creature with flying")
    void birdTokenCanBlockFlyingCreature() {
        Permanent bird = createBird();
        Permanent flyingAttacker = addAttacker(new AirElemental());
        prepareBlockers();
        int birdIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bird);
        int flyingAttackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(flyingAttacker);

        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(birdIndex, flyingAttackerIndex)));

        assertThat(bird.isBlocking()).isTrue();
    }

    private void castRoost() {
        harness.setHand(player1, List.of(new OminousRoost()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent createBird() {
        castRoost();
        return findPermanents(player1, "Bird").getLast();
    }

    private Permanent addAttacker(Card card) {
        Permanent attacker = new Permanent(card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);
        return attacker;
    }

    private void prepareBlockers() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
