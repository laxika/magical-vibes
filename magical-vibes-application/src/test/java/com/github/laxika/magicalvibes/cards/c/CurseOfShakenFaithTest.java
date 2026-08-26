package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.Reverberate;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfShakenFaith.class, Shock.class, CounselOfTheSoratami.class, Reverberate.class})
class CurseOfShakenFaithTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage when the enchanted player casts their second spell")
    void damagesEnchantedPlayerOnSecondSpell() {
        attachCurseToPlayer2();

        Shock first = new Shock();
        Shock second = new Shock();
        harness.setHand(player2, List.of(first, second));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Curse of Shaken Faith"));

        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger for spells cast by the non-enchanted player")
    void ignoresNonEnchantedPlayerSpells() {
        attachCurseToPlayer2();

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Curse of Shaken Faith"));
    }

    @Test
    @DisplayName("Deals damage when the enchanted player copies a spell")
    void damagesEnchantedPlayerWhenTheyCopyASpell() {
        attachCurseToPlayer2();

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        Reverberate reverberate = new Reverberate();
        harness.setHand(player2, List.of(counsel, reverberate));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, 0);
        harness.castInstant(player2, 0, counsel.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);

        harness.passBothPriorities();
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Curse of Shaken Faith"));

        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    private void attachCurseToPlayer2() {
        Permanent curse = new Permanent(new CurseOfShakenFaith());
        curse.setAttachedTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(curse);
    }
}
