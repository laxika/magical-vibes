package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.Earthlore;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LivingLands;
import com.github.laxika.magicalvibes.cards.l.Lure;
import com.github.laxika.magicalvibes.cards.z.Zephid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrownOfTheAges.class, GrizzlyBears.class, Lure.class, Zephid.class,
        Earthlore.class, Forest.class, Island.class, LivingLands.class})
class CrownOfTheAgesTest extends BaseCardTest {

    @Test
    @DisplayName("Activating targets only the Aura")
    void activatingAbilityTargetsOnlyAura() {
        Permanent crown = addCrown(player1);
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAuraAttachedTo(player1, creature1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, aura.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(aura.getId());
        assertThat(entry.getTargetIds()).isEmpty();
        assertThat(crown.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving moves the Aura to another creature chosen during resolution")
    void resolvingMovesAuraToChosenCreature() {
        addCrown(player1);
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature3 = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAuraAttachedTo(player1, creature1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, aura.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(creature2.getId(), creature3.getId())
                .doesNotContain(creature1.getId());

        harness.handlePermanentChosen(player1, creature3.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(creature3.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability fizzles if the Aura leaves the battlefield before resolution")
    void fizzlesIfAuraLeaves() {
        addCrown(player1);
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAuraAttachedTo(player1, creature1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, aura.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(aura.getAttachedTo()).isEqualTo(creature1.getId());
    }

    @Test
    @DisplayName("Aura stays attached when no other creature remains at resolution")
    void staysAttachedWhenNoOtherCreatureRemains() {
        addCrown(player1);
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAuraAttachedTo(player1, creature1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, aura.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(aura.getAttachedTo()).isEqualTo(creature1.getId());
    }

    @Test
    @CardUsed(Zephid.class)
    @DisplayName("Can move the Aura onto a creature with shroud")
    void canMoveAuraOntoShroudedCreature() {
        addCrown(player1);
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent shroudedCreature = addCreatureReady(player1, new Zephid());
        Permanent aura = addAuraAttachedTo(player1, creature1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, aura.getId());
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(shroudedCreature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-Aura permanent as the Aura to move")
    void cannotTargetNonAura() {
        addCrown(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot move an Aura from a creature land to a noncreature land")
    void cannotMoveAuraToNoncreatureLand() {
        addCrown(player1);
        harness.addToBattlefield(player1, new LivingLands());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Earthlore());
        aura.setAttachedTo(forest.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, aura.getId());
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(forest.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an Aura attached to a noncreature land")
    void cannotTargetAuraOnNoncreatureLand() {
        addCrown(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Earthlore());
        aura.setAttachedTo(land.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can move an opponent's Aura onto a creature with a different controller")
    void movesOpponentsAuraWithoutChangingItsController() {
        addCrown(player1);
        Permanent originalHost = addCreatureReady(player2, new GrizzlyBears());
        Permanent newHost = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAuraAttachedTo(player2, originalHost);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, aura.getId());
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(newHost.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(aura);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCrown(Player player) {
        return harness.addToBattlefieldAndReturn(player, new CrownOfTheAges());
    }

    private Permanent addAuraAttachedTo(Player player, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player, new Lure());
        aura.setAttachedTo(host.getId());
        return aura;
    }
}
