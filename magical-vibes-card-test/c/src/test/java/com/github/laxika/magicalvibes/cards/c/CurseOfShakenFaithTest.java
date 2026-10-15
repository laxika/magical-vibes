package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.Reverberate;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
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

    @Test
    void firstSpellOnTheNextTurnDoesNotTrigger() {
        attachCurseToPlayer2();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);

        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void resolvesAsAnAuraEnchantingAPlayer() {
        harness.setHand(player1, List.of(new CurseOfShakenFaith()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Curse of Shaken Faith").getAttachedTo())
                .isEqualTo(player2.getId());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, player1.getId());
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void countsSpellsCastBeforeTheCurseWasAttached() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        attachCurseToPlayer2();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void triggersForTheThirdSpellToo() {
        attachCurseToPlayer2();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    void firstCastCanTriggerByCopyingAnOpponentsSpell() {
        attachCurseToPlayer2();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new Reverberate()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.castInstant(player2, 0, counsel.getId());
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    void ignoresCopiesMadeByTheNonEnchantedPlayer() {
        attachCurseToPlayer2();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(counsel));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new Reverberate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player2, 0, 0);
        harness.castInstant(player1, 0, counsel.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.assertLife(player2, 20);
    }

    @Test
    void canDamageItsOwnControllerForCastingAndCopying() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfShakenFaith());
        curse.setAttachedTo(player1.getId());
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(counsel, new Reverberate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.castInstant(player1, 0, counsel.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    void damageStillResolvesAfterTheCurseLeavesTheBattlefield() {
        attachCurseToPlayer2();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, player1.getId());
        harness.castInstant(player2, 0, player1.getId());
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    private void attachCurseToPlayer2() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfShakenFaith());
        curse.setAttachedTo(player2.getId());
    }
}
