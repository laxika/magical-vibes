package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnholyGrotto.class, Gravecrawler.class, HolyDay.class})
class UnholyGrottoTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapsForColorless() {
        Permanent grotto = addReadyGrotto();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(grotto.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Puts a target Zombie card from the graveyard on top of the library")
    void putsTargetZombieOnTopOfLibrary() {
        Permanent grotto = addReadyGrotto();
        harness.addMana(player1, ManaColor.BLACK, 1);

        Card zombie = new Gravecrawler();
        harness.setGraveyard(player1, List.of(zombie));
        harness.setLibrary(player1, List.of(new HolyDay()));

        int grottoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(grotto);
        harness.activateAbility(player1, grottoIndex, 1, null, zombie.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(zombie);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(zombie);
    }

    @Test
    @DisplayName("Only Zombie cards in your graveyard are legal targets")
    void rejectsNonZombieTarget() {
        Permanent grotto = addReadyGrotto();
        harness.addMana(player1, ManaColor.BLACK, 1);

        Card nonZombie = new HolyDay();
        harness.setGraveyard(player1, List.of(nonZombie));

        int grottoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(grotto);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, grottoIndex, 1, null, nonZombie.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyGrotto() {
        Permanent grotto = harness.addToBattlefieldAndReturn(player1, new UnholyGrotto());
        grotto.setSummoningSick(false);
        return grotto;
    }
}
