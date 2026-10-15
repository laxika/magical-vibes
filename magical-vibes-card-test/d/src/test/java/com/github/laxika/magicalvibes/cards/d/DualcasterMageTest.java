package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.ConeOfFlame;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DualcasterMage.class, CounselOfTheSoratami.class, GrizzlyBears.class,
        Cancel.class, ConeOfFlame.class, Incinerate.class})
class DualcasterMageTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a sorcery spell when it enters the battlefield")
    void copiesSorceryOnEnter() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        GrizzlyBears firstDraw = new GrizzlyBears();
        GrizzlyBears secondDraw = new GrizzlyBears();

        harness.setHand(player1, List.of(counsel));
        harness.setLibrary(player2, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new DualcasterMage()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, counsel.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player2.getId()))
                .containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Does not trigger when no instant or sorcery spell is on the stack")
    void doesNotTargetCreatureSpell() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new DualcasterMage()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Copying an instant preserves its target when new targets are declined")
    void keepsInstantTarget() {
        Incinerate incinerate = new Incinerate();
        harness.setHand(player1, List.of(incinerate));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new DualcasterMage()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, incinerate.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(incinerate);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The copy can target a different player without changing the original spell")
    void changesInstantTarget() {
        Incinerate incinerate = new Incinerate();
        harness.setHand(player1, List.of(incinerate));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new DualcasterMage()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, incinerate.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("No copy is created when the targeted spell is countered before the trigger resolves")
    void targetedSpellLeavesStack() {
        Incinerate incinerate = new Incinerate();
        harness.setHand(player1, List.of(incinerate, new Cancel()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new DualcasterMage()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, incinerate.getId());
        harness.castAndResolveInstant(player1, 0, incinerate.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Dualcaster Mage");
    }

    @Test
    @DisplayName("A later target of a copied spell can change while the other targets remain unchanged")
    void changesSecondTargetOfCopy() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        GrizzlyBears replacement = new GrizzlyBears();
        harness.addToBattlefield(player1, first);
        harness.addToBattlefield(player1, second);
        harness.addToBattlefield(player1, third);
        harness.addToBattlefield(player1, replacement);
        List<UUID> targets = gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getId()).toList();
        ConeOfFlame cone = new ConeOfFlame();
        harness.setHand(player1, List.of(cone));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setHand(player2, List.of(new DualcasterMage()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, targets.subList(0, 3));
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, cone.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, targets.get(0));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player2, targets.get(3));
        harness.handlePermanentChosen(player2, targets.get(2));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard()).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(third, replacement);
    }
}
