package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseAnotherCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DauntlessBodyguardTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ChooseAnotherCreatureOnEnterEffect in ETB slot")
    void hasChooseCreatureOnEnterEffect() {
        DauntlessBodyguard card = new DauntlessBodyguard();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ChooseAnotherCreatureOnEnterEffect.class);
    }

    @Test
    @DisplayName("Has sacrifice ability with GrantKeywordToChosenCreatureUntilEndOfTurnEffect")
    void hasSacrificeAbility() {
        DauntlessBodyguard card = new DauntlessBodyguard();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(GrantKeywordToChosenCreatureUntilEndOfTurnEffect.class);
        GrantKeywordToChosenCreatureUntilEndOfTurnEffect grant =
                (GrantKeywordToChosenCreatureUntilEndOfTurnEffect) ability.getEffects().get(1);
        assertThat(grant.keyword()).isEqualTo(Keyword.INDESTRUCTIBLE);
        assertThat(grant.chosenCreatureId()).isNull();
    }

    // ===== ETB: choose another creature =====

    @Test
    @DisplayName("Casting with another creature prompts for creature choice")
    void castingWithOtherCreaturePromptsChoice() {
        Permanent bears = addReadyCreature(player1);
        harness.setHand(player1, List.of(new DauntlessBodyguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.PERMANENT_CHOICE)).isTrue();
    }

    @Test
    @DisplayName("Choosing a creature stores chosenPermanentId on the bodyguard")
    void choosingCreatureStoresId() {
        Permanent bears = addReadyCreature(player1);
        harness.setHand(player1, List.of(new DauntlessBodyguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bears.getId());

        Permanent bodyguard = findPermanent(player1, "Dauntless Bodyguard");
        assertThat(bodyguard.getChosenPermanentId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Casting with no other creatures enters without choice prompt")
    void castingWithNoOtherCreaturesEntersNormally() {
        harness.setHand(player1, List.of(new DauntlessBodyguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.PERMANENT_CHOICE)).isFalse();
        Permanent bodyguard = findPermanent(player1, "Dauntless Bodyguard");
        assertThat(bodyguard).isNotNull();
        assertThat(bodyguard.getChosenPermanentId()).isNull();
    }

    // ===== Sacrifice ability: grant indestructible =====

    @Test
    @DisplayName("Sacrificing bodyguard grants indestructible to chosen creature")
    void sacrificeGrantsIndestructibleToChosenCreature() {
        Permanent bears = addReadyCreature(player1);
        Permanent bodyguard = addReadyBodyguard(player1);
        bodyguard.setChosenPermanentId(bears.getId());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Dauntless Bodyguard"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dauntless Bodyguard"));
    }

    @Test
    @DisplayName("Sacrifice does nothing if no creature was chosen")
    void sacrificeDoesNothingWhenNoCreatureChosen() {
        Permanent bears = addReadyCreature(player1);
        Permanent bodyguard = addReadyBodyguard(player1);
        // chosenPermanentId is null (no creature chosen)

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Dauntless Bodyguard"));
    }

    @Test
    @DisplayName("Sacrifice does nothing if chosen creature left the battlefield")
    void sacrificeDoesNothingWhenChosenCreatureGone() {
        Permanent bears = addReadyCreature(player1);
        Permanent bodyguard = addReadyBodyguard(player1);
        bodyguard.setChosenPermanentId(bears.getId());

        // Remove the chosen creature before activating
        gd.playerBattlefields.get(player1.getId()).remove(bears);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Bodyguard was sacrificed but chosen creature is gone — nothing to grant indestructible to
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Dauntless Bodyguard"));
    }

    // ===== Full flow: cast + choose + sacrifice =====

    @Test
    @DisplayName("Full flow: cast bodyguard, choose creature, sacrifice for indestructible")
    void fullFlowCastChooseSacrifice() {
        Permanent bears = addReadyCreature(player1);
        harness.setHand(player1, List.of(new DauntlessBodyguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Cast bodyguard
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Choose bears as the protected creature
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent bodyguard = findPermanent(player1, "Dauntless Bodyguard");
        assertThat(bodyguard.getChosenPermanentId()).isEqualTo(bears.getId());

        // Sacrifice bodyguard
        bodyguard.setSummoningSick(false);
        int bodyguardIdx = gd.playerBattlefields.get(player1.getId()).indexOf(bodyguard);
        harness.activateAbility(player1, bodyguardIdx, null, null);
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Dauntless Bodyguard"));
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBodyguard(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = new Permanent(new DauntlessBodyguard());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

}
