package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.q.Quasiduplicate;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GleamingOverseer.class, PrimordialWurm.class, Quasiduplicate.class})
class GleamingOverseerTest extends BaseCardTest {

    @Test
    void amassesWithoutAnArmyAndGrantsKeywordsToZombieArmyToken() {
        castGleamingOverseer();

        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, army)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, army)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, army, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, army, Keyword.MENACE)).isTrue();
    }

    @Test
    void amassesOnExistingArmyWithoutGrantingKeywordsToNontokenZombie() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        PermanentGrants.subtype(gd, army, CardSubtype.ARMY);

        castGleamingOverseer();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(PermanentGrants.subtypesOf(gd, army)).contains(CardSubtype.ZOMBIE);
        assertThat(gqs.hasKeyword(gd, army, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, army, Keyword.MENACE)).isFalse();
    }

    @Test
    void choosesOnlyOneOfMultipleArmiesAndMakesItAZombie() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        PermanentGrants.subtype(gd, first, CardSubtype.ARMY);
        PermanentGrants.subtype(gd, second, CardSubtype.ARMY);

        castGleamingOverseer();
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(PermanentGrants.subtypesOf(gd, first)).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(PermanentGrants.subtypesOf(gd, second)).contains(CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
    }

    @Test
    void repeatedAmassAddsToExistingTokenAndKeywordsDisappearWithOverseers() {
        castGleamingOverseer();
        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        castGleamingOverseer();

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> !permanent.getCard().isToken());
        assertThat(gqs.hasKeyword(gd, army, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, army, Keyword.MENACE)).isFalse();
    }

    @Test
    void opposingArmyDoesNotPreventCreationOrReceiveKeywords() {
        Permanent opposingArmy = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        PermanentGrants.subtype(gd, opposingArmy, CardSubtype.ARMY);

        castGleamingOverseer();
        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(army);
        gd.playerBattlefields.get(player2.getId()).add(army);

        assertThat(opposingArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, army, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, army, Keyword.MENACE)).isFalse();
    }

    @Test
    void tokenCopyGrantsItselfHexproofAndMenace() {
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new GleamingOverseer());
        harness.setHand(player1, List.of(new Quasiduplicate()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0, overseer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Gleaming Overseer"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(overseer);

        assertThat(gqs.hasKeyword(gd, copy, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, copy, Keyword.MENACE)).isTrue();
    }

    private void castGleamingOverseer() {
        harness.castFromHand(player1, new GleamingOverseer(), "{1}{U}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
