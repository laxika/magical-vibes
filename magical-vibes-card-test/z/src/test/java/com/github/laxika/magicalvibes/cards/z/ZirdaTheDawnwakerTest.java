package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.a.AmaranthineWall;
import com.github.laxika.magicalvibes.cards.f.FiendArtisan;
import com.github.laxika.magicalvibes.cards.f.FrillscareMentor;
import com.github.laxika.magicalvibes.cards.g.GreaterSandwurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Prismite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZirdaTheDawnwaker.class, AmaranthineWall.class, Prismite.class, GrizzlyBears.class, FrillscareMentor.class, GreaterSandwurm.class,
        FiendArtisan.class})
class ZirdaTheDawnwakerTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces a two-mana ability to the one-mana minimum")
    void reducesNonManaActivatedAbility() {
        harness.addToBattlefield(player1, new ZirdaTheDawnwaker());
        harness.addToBattlefield(player1, new AmaranthineWall());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not reduce a mana ability")
    void doesNotReduceManaAbility() {
        harness.addToBattlefield(player1, new ZirdaTheDawnwaker());
        harness.addToBattlefield(player1, new Prismite());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.handleListChoice(player1, "GREEN");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Makes the targeted creature unable to block this turn")
    void targetCreatureCannotBlock() {
        Permanent zirda = addCreatureReady(player1, new ZirdaTheDawnwaker());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        declareAttackersAndPrepareBlockers(player1, List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
        assertThat(zirda.isTapped()).isTrue();
        assertThat(attacker.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Reduces two generic mana while preserving colored mana and the tap cost")
    void reducesTwoGenericManaButNotColoredMana() {
        harness.addToBattlefield(player1, new ZirdaTheDawnwaker());
        Permanent mentor = addCreatureReady(player1, new FrillscareMentor());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(mentor.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not reduce an opponent's activated ability")
    void doesNotReduceOpponentsAbility() {
        harness.addToBattlefield(player1, new ZirdaTheDawnwaker());
        addCreatureReady(player2, new FrillscareMentor());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Zirda's own one-mana ability cannot be activated for free")
    void ownAbilityStillRequiresOneMana() {
        Permanent zirda = addCreatureReady(player1, new ZirdaTheDawnwaker());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, zirda.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(zirda.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target a creature controlled by its controller")
    void canTargetOwnCreature() {
        Permanent zirda = addCreatureReady(player1, new ZirdaTheDawnwaker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, zirda.getId());
        harness.passBothPriorities();

        assertThat(zirda.isCantBlockThisTurn()).isTrue();
        assertThat(zirda.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Reduces cycling from hand to one mana")
    void reducesCyclingFromHand() {
        harness.addToBattlefield(player1, new ZirdaTheDawnwaker());
        harness.setHand(player1, List.of(new GreaterSandwurm()));
        harness.setLibrary(player1, List.of(new FrillscareMentor()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Greater Sandwurm");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Frillscare Mentor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reduces the generic mana represented by X without changing X")
    void reducesGenericManaInXCost() {
        Permanent artisan = addCreatureReady(player1, new FiendArtisan());
        addCreatureReady(player1, new ZirdaTheDawnwaker());
        harness.setLibrary(player1, List.of(new FiendArtisan()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null);

        assertThat(artisan.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Zirda, the Dawnwaker");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(countPermanents(player1, "Fiend Artisan")).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
