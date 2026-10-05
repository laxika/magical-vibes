package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ConeOfFlame;
import com.github.laxika.magicalvibes.cards.f.FlameJab;
import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirrorSheen.class, Shock.class, FlameJab.class, ConeOfFlame.class, NettleSentinel.class})
class MirrorSheenTest extends BaseCardTest {

    private int prepareSheen() {
        Permanent sheen = harness.addToBattlefieldAndReturn(player2, new MirrorSheen());
        harness.addMana(player2, ManaColor.BLUE, 3);
        return gd.playerBattlefields.get(player2.getId()).indexOf(sheen);
    }

    @Test
    @DisplayName("Copies an instant spell that targets you")
    void copiesInstantTargetingYou() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        int sheenIdx = prepareSheen();

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        harness.activateAbility(player2, sheenIdx, null, shock.getId());
        harness.passBothPriorities();

        StackEntry copy = gd.stack.getLast();
        assertThat(copy.isCopy()).isTrue();
        assertThat(copy.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(copy.getControllerId()).isEqualTo(player2.getId());
        assertThat(copy.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The copy resolves, dealing the spell's damage a second time")
    void copyDealsDamageAgain() {
        harness.setLife(player2, 20);
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        int sheenIdx = prepareSheen();

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        harness.activateAbility(player2, sheenIdx, null, shock.getId());
        harness.passBothPriorities();                    // ability resolves -> copy created
        harness.handleMayAbilityChosen(player2, false);  // keep the copy's target (player2)
        harness.passBothPriorities();                    // copy resolves -> 2 damage
        harness.passBothPriorities();                    // original Shock resolves -> 2 damage

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cannot copy a spell that targets an opponent instead of you")
    void cannotCopySpellTargetingOpponent() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        int sheenIdx = prepareSheen();

        harness.castInstant(player1, 0, player1.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, sheenIdx, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Copies a sorcery that targets you using red hybrid mana")
    void copiesSorceryWithRedMana() {
        FlameJab jab = new FlameJab();
        harness.setHand(player1, List.of(jab));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addToBattlefield(player2, new MirrorSheen());
        harness.addMana(player2, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, jab.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(jab);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The copy may target an opponent while the original keeps its target")
    void retargetsCopyToOpponent() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        int sheenIdx = prepareSheen();

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, sheenIdx, null, shock.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());

        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Can copy your own spell when it targets you")
    void copiesOwnSpellTargetingYou() {
        FlameJab jab = new FlameJab();
        harness.setHand(player1, List.of(jab));
        harness.addToBattlefield(player1, new MirrorSheen());
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, player1.getId());
        harness.activateAbility(player1, 0, null, jab.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Can activate again to copy a spell copy that still targets you")
    void copiesCopyTargetingYou() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);
        int sheenIdx = prepareSheen();
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, sheenIdx, null, shock.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        StackEntry firstCopy = gd.stack.getLast();
        harness.activateAbility(player2, sheenIdx, null, firstCopy.getTargetableId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack.getLast().isCopy()).isTrue();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Allows changing later targets of a copy with multiple targets")
    void retargetsMultipleTargetsOfCopy() {
        ConeOfFlame cone = new ConeOfFlame();
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new NettleSentinel());
        harness.setHand(player1, List.of(cone));
        harness.addMana(player1, ManaColor.RED, 5);
        int sheenIdx = prepareSheen();

        harness.castSorcery(player1, 0, List.of(player1.getId(), player2.getId(), sentinel.getId()));
        harness.passPriority(player1);
        harness.activateAbility(player2, sheenIdx, null, cone.getId());
        harness.passBothPriorities();

        StackEntry copy = gd.stack.getLast();
        assertThat(copy.isCopy()).isTrue();
        assertThat(copy.getTargetIds()).containsExactly(player1.getId(), player2.getId(), sentinel.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player2, sentinel.getId());
        harness.handlePermanentChosen(player2, player2.getId());

        assertThat(copy.getTargetIds()).containsExactly(player1.getId(), sentinel.getId(), player2.getId());
        assertThat(gd.stack.getFirst().getTargetIds())
                .containsExactly(player1.getId(), player2.getId(), sentinel.getId());
    }
}
