package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UyoSilentProphet.class, CounselOfTheSoratami.class, GlacialRay.class,
        HumbleBudoka.class, Island.class})
class UyoSilentProphetTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two lands as a cost and copies a sorcery spell you control")
    void copiesOwnSpellAndReturnsTwoLands() {
        harness.addToBattlefield(player1, new UyoSilentProphet());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, uyoIndex(player1), null, counsel.getId(), Zone.STACK);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Island"));
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Island")).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        StackEntry copy = gd.stack.stream().filter(StackEntry::isCopy).findFirst().orElseThrow();
        assertThat(copy.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(copy.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Can copy a spell controlled by another player")
    void copiesOpponentSpell() {
        harness.addToBattlefield(player1, new UyoSilentProphet());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player2, List.of(counsel));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0);
        harness.activateAbility(player1, uyoIndex(player1), null, counsel.getId(), Zone.STACK);
        harness.passBothPriorities();

        StackEntry copy = gd.stack.stream().filter(StackEntry::isCopy).findFirst().orElseThrow();
        assertThat(copy.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot copy a creature spell")
    void cannotCopyCreatureSpell() {
        harness.addToBattlefield(player1, new UyoSilentProphet());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        HumbleBudoka budoka = new HumbleBudoka();
        harness.setHand(player1, List.of(budoka));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        UUID budokaId = budoka.getId();
        int index = uyoIndex(player1);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, budokaId, Zone.STACK))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with only one land to return")
    void cannotActivateWithOneLand() {
        harness.addToBattlefield(player1, new UyoSilentProphet());
        harness.addToBattlefield(player1, new Island());

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);

        UUID counselId = counsel.getId();
        int index = uyoIndex(player1);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, counselId, Zone.STACK))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Copies an instant and can retarget the copy")
    void copiesInstantAndCanRetargetCopy() {
        harness.addToBattlefield(player1, new UyoSilentProphet());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        GlacialRay ray = new GlacialRay();
        harness.setHand(player1, List.of(ray));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, player1.getId());
        harness.activateAbility(player1, uyoIndex(player1), null, ray.getId(), Zone.STACK);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        StackEntry copy = gd.stack.stream().filter(StackEntry::isCopy).findFirst().orElseThrow();
        assertThat(copy.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(copy.getTargetId()).isEqualTo(player2.getId());

        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot use an opponent's land to pay the return cost")
    void cannotUseOpponentsLandAsCost() {
        harness.addToBattlefield(player1, new UyoSilentProphet());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, uyoIndex(player1), null, counsel.getId(), Zone.STACK))
                .isInstanceOf(IllegalStateException.class);
    }

    private int uyoIndex(Player owner) {
        return gd.playerBattlefields.get(owner.getId())
                .indexOf(findPermanent(owner, "Uyo, Silent Prophet"));
    }
}
