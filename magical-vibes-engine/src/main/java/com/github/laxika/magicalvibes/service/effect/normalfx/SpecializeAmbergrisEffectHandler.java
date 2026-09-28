package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.a.AmbergrisCitadelAgent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeAmbergrisEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SpecializeAmbergrisEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeAmbergrisEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var specialize = (SpecializeAmbergrisEffect) effect;
        var source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Ambergris, Citadel Agent".equals(source.getCard().getName())) {
            return;
        }

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        AmbergrisCitadelAgent.setBaseFaceCharacteristics(specialized);

        switch (specialize.color()) {
            case WHITE -> {
                specialized.setName("Ambergris, Agent of Law");
                specialized.setManaCost("{2}{R}{W}");
                specialized.setColors(List.of(CardColor.RED, CardColor.WHITE));
                specialized.setColorIdentity(List.of(CardColor.RED, CardColor.WHITE));
                specialized.setColor(CardColor.WHITE);
                specialized.setPower(4);
                specialized.setToughness(3);
                specialized.setCardText("Haste\nWhenever Ambergris, Agent of Law attacks, you may discard your hand and draw two cards. If you do, other creatures you control get +X/+X until end of turn, where X is the number of cards you've discarded this turn.");
            }
            case BLUE -> {
                specialized.setName("Ambergris, Agent of Progress");
                specialized.setManaCost("{2}{U}{R}");
                specialized.setColors(List.of(CardColor.BLUE, CardColor.RED));
                specialized.setColorIdentity(List.of(CardColor.BLUE, CardColor.RED));
                specialized.setColor(CardColor.BLUE);
                specialized.setPower(4);
                specialized.setToughness(3);
                specialized.setCardText("Haste\nWhenever Ambergris, Agent of Progress attacks, you may discard your hand and draw three cards.");
            }
            case BLACK -> {
                specialized.setName("Ambergris, Agent of Tyranny");
                specialized.setManaCost("{2}{B}{R}");
                specialized.setColors(List.of(CardColor.BLACK, CardColor.RED));
                specialized.setColorIdentity(List.of(CardColor.BLACK, CardColor.RED));
                specialized.setColor(CardColor.BLACK);
                specialized.setPower(4);
                specialized.setToughness(3);
                specialized.setCardText("Haste\nWhenever Ambergris, Agent of Tyranny attacks, you may discard your hand and draw two cards. When you do, target creature an opponent controls gets -X/-X until end of turn, where X is the number of cards you've discarded this turn.");
            }
            case RED -> {
                specialized.setName("Ambergris, Agent of Destruction");
                specialized.setManaCost("{2}{R}{R}");
                specialized.setColors(List.of(CardColor.RED));
                specialized.setColorIdentity(List.of(CardColor.RED));
                specialized.setColor(CardColor.RED);
                specialized.setPower(4);
                specialized.setToughness(3);
                specialized.setCardText("Haste\nWhenever Ambergris, Agent of Destruction attacks, you may discard your hand and draw two cards. If you do, Ambergris deals X damage to each opponent, where X is the number of cards you've discarded this turn.");
            }
            case GREEN -> {
                specialized.setName("Ambergris, Agent of Balance");
                specialized.setManaCost("{2}{R}{G}");
                specialized.setColors(List.of(CardColor.RED, CardColor.GREEN));
                specialized.setColorIdentity(List.of(CardColor.RED, CardColor.GREEN));
                specialized.setColor(CardColor.GREEN);
                specialized.setPower(4);
                specialized.setToughness(3);
                specialized.setCardText("Haste\nWhenever Ambergris, Agent of Balance attacks, you may discard your hand and draw two cards. When you do, put X +1/+1 counters on another target creature you control, where X is the number of cards you've discarded this turn.");
            }
            default -> throw new IllegalStateException("Unsupported Ambergris specialization color: "
                    + specialize.color());
        }

        specialized.addEffect(EffectSlot.ON_ATTACK,
                AmbergrisCitadelAgent.specializedAttackAbility(specialize.color()));
        source.exchangeCard(specialized);
    }
}
