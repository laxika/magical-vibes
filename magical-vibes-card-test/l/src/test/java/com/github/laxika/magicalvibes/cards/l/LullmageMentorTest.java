package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BurstLightning;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.m.MerfolkSeastalkers;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LullmageMentor.class, MerfolkSeastalkers.class, BurstLightning.class, Cancel.class})
class LullmageMentorTest extends BaseCardTest {

    @Test
    void countersSpellByTappingSevenMerfolkAndMayCreateToken() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new LullmageMentor());
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new MerfolkSeastalkers());
        }

        BurstLightning shock = castOpposingSpell();

        int mentorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mentor);
        harness.activateAbility(player1, mentorIndex, null, shock.getId());

        List<UUID> merfolkIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.MERFOLK))
                .map(Permanent::getId)
                .toList();
        for (UUID merfolkId : merfolkIds.subList(0, 7)) {
            harness.handlePermanentChosen(player1, merfolkId);
        }

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.MERFOLK));
    }

    @Test
    void canTapExactlySevenSummoningSickMerfolkIncludingMentorAndDeclineToken() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new LullmageMentor());
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new MerfolkSeastalkers());
        }
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(true));
        BurstLightning spell = castOpposingSpell();

        harness.activateAbility(player1, 0, null, spell.getId());

        assertThat(mentor.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spell);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(7)
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    void cannotActivateWithOnlySixUntappedMerfolkEvenWhenOpponentHasMerfolk() {
        harness.addToBattlefield(player1, new LullmageMentor());
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new MerfolkSeastalkers());
        }
        gd.playerBattlefields.get(player1.getId()).getLast().tap();
        harness.addToBattlefield(player2, new MerfolkSeastalkers());
        BurstLightning spell = castOpposingSpell();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream().filter(Permanent::isTapped))
                .hasSize(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void counterspellTriggersOnlyMentorsControlledByCounteringPlayer() {
        harness.addToBattlefield(player1, new LullmageMentor());
        harness.addToBattlefield(player2, new LullmageMentor());
        BurstLightning spell = castOpposingSpell();
        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, spell.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spell);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1)
                .noneMatch(p -> p.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }

    private BurstLightning castOpposingSpell() {
        BurstLightning spell = new BurstLightning();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        return spell;
    }
}
