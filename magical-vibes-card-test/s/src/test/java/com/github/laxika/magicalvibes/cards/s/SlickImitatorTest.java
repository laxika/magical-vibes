package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.b.BounceOff;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlickImitator.class, AngelsMercy.class, BounceOff.class})
class SlickImitatorTest extends BaseCardTest {

    @Test
    void atMaxSpeedSacrificesAndCopiesOwnSpell() {
        Permanent imitator = addCreatureReady(player1, new SlickImitator());
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerSpeeds.put(player1.getId(), 4);

        harness.castInstant(player1, 0);
        harness.activateAbility(player1, 0, null, mercy.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(imitator);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .singleElement()
                .satisfies(copy -> assertThat(copy.getDescription()).isEqualTo("Copy of Angel's Mercy"));
    }

    @Test
    void cannotActivateBelowMaxSpeed() {
        addCreatureReady(player1, new SlickImitator());
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerSpeeds.put(player1.getId(), 3);

        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mercy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentSpell() {
        addCreatureReady(player1, new SlickImitator());
        AngelsMercy mercy = new AngelsMercy();
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(mercy));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castInstant(player2, 0);

        gd.playerSpeeds.put(player1.getId(), 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mercy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enteringStartsSpeedWithoutResettingExistingSpeed() {
        harness.setHand(player1, List.of(new SlickImitator(), new SlickImitator()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);

        gd.playerSpeeds.put(player1.getId(), 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void copiedPermanentSpellResolvesAsToken() {
        addCreatureReady(player1, new SlickImitator());
        SlickImitator spell = new SlickImitator();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerSpeeds.put(player1.getId(), 4);

        harness.castCreature(player1, 0);
        harness.activateAbility(player1, 0, null, spell.getId());
        harness.assertInGraveyard(player1, "Slick Imitator");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void canChooseNewTargetForCopyWithoutChangingOriginal() {
        addCreatureReady(player1, new SlickImitator());
        Permanent originalTarget = addCreatureReady(player2, new SlickImitator());
        Permanent newTarget = addCreatureReady(player2, new SlickImitator());
        BounceOff spell = new BounceOff();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerSpeeds.put(player1.getId(), 4);

        harness.castInstant(player1, 0, originalTarget.getId());
        harness.activateAbility(player1, 0, null, spell.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, newTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(originalTarget).doesNotContain(newTarget);
        assertThat(gd.playerHands.get(player2.getId())).contains(newTarget.getCard());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(originalTarget);
        assertThat(gd.playerHands.get(player2.getId())).contains(originalTarget.getCard());
    }
}
