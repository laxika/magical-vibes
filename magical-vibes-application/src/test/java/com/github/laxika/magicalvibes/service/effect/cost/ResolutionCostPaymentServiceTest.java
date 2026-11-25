package com.github.laxika.magicalvibes.service.effect.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResolutionCostPaymentServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private LifeSupport lifeSupport;

    private ResolutionCostPaymentService service;
    private GameData gd;
    private UUID payerId;

    @BeforeEach
    void setUp() {
        payerId = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", payerId, "Payer");
        gd.playerIds.add(payerId);
        gd.playerManaPools.put(payerId, new ManaPool());
        gd.playerLifeTotals.put(payerId, 5);
        service = new ResolutionCostPaymentService(gameQueryService, lifeSupport);
    }

    @Test
    @DisplayName("0 life can always be paid, even by a player who can't lose life (CR 119.4)")
    void zeroLifeIsAlwaysPayable() {
        assertThat(service.canPayLife(gd, payerId, 0)).isTrue();
    }

    @Test
    @DisplayName("A player who can't lose life can't pay life, though their life total covers it (CR 119.8)")
    void playerWhoCantLoseLifeCantPay() {
        when(gameQueryService.canPlayerLoseLife(gd, payerId)).thenReturn(false);

        assertThat(service.canPay(gd, payerId, new PayLifeCost(3), null)).isFalse();
    }

    @Test
    @DisplayName("Life greater than the payer's life total can't be paid (CR 119.4)")
    void lifeAboveTheLifeTotalCantBePaid() {
        when(gameQueryService.canPlayerLoseLife(gd, payerId)).thenReturn(true);

        assertThat(service.canPay(gd, payerId, new PayLifeCost(5), null)).isTrue();
        assertThat(service.canPay(gd, payerId, new PayLifeCost(6), null)).isFalse();
    }

    @Test
    @DisplayName("Mana the pool can't cover is not paid at all (CR 118.3)")
    void unpayableManaPaysNothing() {
        gd.playerManaPools.get(payerId).add(ManaColor.GREEN, 1);

        assertThat(service.canPay(gd, payerId, new PayManaCost("{2}"), null)).isFalse();
        assertThat(service.payManaFromPool(gd, payerId, new ManaCost("{2}"), UUID.randomUUID())).isFalse();
        assertThat(gd.playerManaPools.get(payerId).get(ManaColor.GREEN)).isOne();
    }

    @Test
    @DisplayName("Mana the pool covers is paid from it")
    void payableManaIsPaid() {
        gd.playerManaPools.get(payerId).add(ManaColor.GREEN, 2);

        assertThat(service.canPay(gd, payerId, new PayManaCost("{2}"), null)).isTrue();
        assertThat(service.payManaFromPool(gd, payerId, new ManaCost("{2}"), UUID.randomUUID())).isTrue();
        assertThat(gd.playerManaPools.get(payerId).getTotalAllMana()).isZero();
    }
}
