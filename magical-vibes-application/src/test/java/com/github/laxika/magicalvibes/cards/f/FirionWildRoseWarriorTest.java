package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirionWildRoseWarrior.class, GrizzlyBears.class, LeoninScimitar.class})
class FirionWildRoseWarriorTest extends BaseCardTest {

    @Test
    void equippedCreaturesYouControlHaveHaste() {
        Permanent firion = addCreatureReady(player1, new FirionWildRoseWarrior());
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        scimitar.setAttachedTo(equippedCreature.getId());

        assertThat(gqs.hasKeyword(gd, equippedCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, firion, Keyword.HASTE)).isFalse();
    }

    @Test
    void createsReducedEquipCostTokenCopyForNontokenEquipment() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new FirionWildRoseWarrior());
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);

        harness.activateAbility(player1, tokenIndex, 0, target.getId());
        harness.passBothPriorities();

        assertThat(token.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void sacrificesTokenAtBeginningOfNextUpkeep() {
        addCreatureReady(player1, new FirionWildRoseWarrior());
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());

        advanceToUpkeep(player2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }
}
