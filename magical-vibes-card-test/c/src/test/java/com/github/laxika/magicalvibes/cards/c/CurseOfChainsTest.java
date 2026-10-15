package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DevotedDruid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfChains.class, DevotedDruid.class, ConsignToDream.class})
class CurseOfChainsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Curse of Chains attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creaturePerm = addCreatureReady(player1, new DevotedDruid());

        harness.setHand(player1, List.of(new CurseOfChains()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, creaturePerm.getId());
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        harness.passBothPriorities();

        assertThat(creaturePerm.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse of Chains")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creaturePerm.getId()));
    }

    @Test
    @DisplayName("Taps the enchanted creature during upkeep")
    void tapsEnchantedCreatureDuringUpkeep() {
        Permanent creaturePerm = addCreatureReady(player1, new DevotedDruid());
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new CurseOfChains());
        auraPerm.setAttachedTo(creaturePerm.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(creaturePerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps the enchanted creature during each player's upkeep")
    void tapsDuringEachUpkeep() {
        Permanent creaturePerm = addCreatureReady(player1, new DevotedDruid());
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new CurseOfChains());
        auraPerm.setAttachedTo(creaturePerm.getId());

        // Opponent's upkeep also triggers it
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(creaturePerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps an opponent's enchanted creature during upkeep")
    void tapsOpponentEnchantedCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new DevotedDruid());
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new CurseOfChains());
        auraPerm.setAttachedTo(opponentCreature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(opponentCreature.isTapped()).isTrue();
    }
    @Test
    @DisplayName("Upkeep trigger taps the creature even if the Aura is bounced in response")
    void tapsAfterAuraLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new DevotedDruid());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CurseOfChains());
        aura.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new ConsignToDream()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isTrue();
    }
}
