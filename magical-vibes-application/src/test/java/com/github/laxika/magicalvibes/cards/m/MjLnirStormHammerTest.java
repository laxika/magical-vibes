package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MjLnirStormHammer.class, GrizzlyBears.class, IsamaruHoundOfKonda.class})
class MjLnirStormHammerTest extends BaseCardTest {

    @Test
    @DisplayName("Mjölnir enters attached to a target legendary creature you control")
    void entersAttachedToLegendaryCreature() {
        Permanent legendary = addCreatureReady(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new MjLnirStormHammer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, legendary.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent hammer = findHammer(player1);
        assertThat(hammer.getAttachedTo()).isEqualTo(legendary.getId());
    }

    @Test
    @DisplayName("ETB attachment rejects a nonlegendary creature")
    void entersCannotTargetNonlegendaryCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MjLnirStormHammer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    @DisplayName("Equipped creature attacks by tapping and stunning a defending creature, then damages each opponent for their tapped creatures")
    void attacksTapStunAndDamageForTappedCreatures() {
        Permanent attacker = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent hammer = addHammerReady(player1);
        hammer.setAttachedTo(attacker.getId());
        Permanent alreadyTapped = addCreatureReady(player2, new GrizzlyBears());
        alreadyTapped.tap();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactlyInAnyOrder(alreadyTapped.getId(), target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Equip {4} can attach Mjölnir to any creature you control")
    void equipAttachesToCreatureYouControl() {
        Permanent hammer = addHammerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hammer.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addHammerReady(Player player) {
        Permanent hammer = new Permanent(new MjLnirStormHammer());
        hammer.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(hammer);
        return hammer;
    }

    private Permanent findHammer(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof MjLnirStormHammer)
                .findFirst()
                .orElseThrow();
    }
}
