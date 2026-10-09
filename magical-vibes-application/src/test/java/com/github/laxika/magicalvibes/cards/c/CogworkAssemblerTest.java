package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DruidOfTheCowl;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SculptingSteel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CogworkAssembler.class, Ornithopter.class, DruidOfTheCowl.class, SculptingSteel.class})
class CogworkAssemblerTest extends BaseCardTest {

    @Test
    void createsHastyTokenCopyOfTargetArtifactAndExilesItAtNextEndStep() {
        Permanent assembler = addReadyAssembler();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(assembler.isTapped()).isFalse();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void cannotTargetNonArtifactPermanent() {
        addReadyAssembler();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DruidOfTheCowl());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    void canActivateWhileSummoningSick() {
        Permanent assembler = harness.addToBattlefieldAndReturn(player1, new CogworkAssembler());
        assembler.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, assembler.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(assembler.isTapped()).isFalse();
    }

    @Test
    void canActivateWhileTapped() {
        Permanent assembler = addReadyAssembler();
        assembler.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, assembler.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(assembler.isTapped()).isTrue();
    }

    @Test
    void canActivateRepeatedlyWithoutUntapping() {
        Permanent assembler = addReadyAssembler();
        harness.addMana(player1, ManaColor.COLORLESS, 14);

        harness.activateAbility(player1, 0, null, assembler.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, assembler.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    @Test
    void grantedHasteIsNotCopiedBySculptingSteel() {
        addReadyAssembler();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, token.getId());

        Permanent steel = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof SculptingSteel)
                .findFirst().orElseThrow();
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(steel.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(steel.hasKeyword(Keyword.HASTE)).isFalse();
    }

    private Permanent addReadyAssembler() {
        Permanent assembler = harness.addToBattlefieldAndReturn(player1, new CogworkAssembler());
        assembler.setSummoningSick(false);
        return assembler;
    }
}
