package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.i.ImoenTricksterFriend;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeImoenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Applies Imoen's five digital specialized faces to the permanent's runtime card. */
@Component
@RequiredArgsConstructor
public class SpecializeImoenEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeImoenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SpecializeImoenEffect specialize = (SpecializeImoenEffect) effect;
        var source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Imoen, Trickster Friend".equals(source.getCard().getName())) {
            return;
        }

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        ImoenTricksterFriend.setSpecializedBaseCharacteristics(specialized);
        specialized.setPower(3);
        specialized.setToughness(2);
        specialized.addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());

        switch (specialize.color()) {
            case WHITE -> {
                specialized.setName("Imoen, Honorable Trickster");
                specialized.setManaCost("{1}{W}{U}");
                specialized.setColors(List.of(CardColor.WHITE, CardColor.BLUE));
                specialized.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLUE));
                specialized.setColor(CardColor.WHITE);
                specialized.setCardText("Imoen can't be blocked. Whenever Imoen deals combat damage to a player, you may exile an instant or sorcery card from your graveyard. If you do, put a +1/+1 counter on each creature you control.");
            }
            case BLUE -> {
                specialized.setName("Imoen, Wily Trickster");
                specialized.setManaCost("{1}{U}{U}");
                specialized.setCardText("Imoen can't be blocked. Whenever Imoen deals combat damage to a player, you may exile an instant or sorcery card from your graveyard. If you do, tap target creature an opponent controls. That creature doesn't untap during its controller's next untap step.");
            }
            case BLACK -> {
                specialized.setName("Imoen, Occult Trickster");
                specialized.setManaCost("{1}{U}{B}");
                specialized.setColors(List.of(CardColor.BLUE, CardColor.BLACK));
                specialized.setColorIdentity(List.of(CardColor.BLUE, CardColor.BLACK));
                specialized.setColor(CardColor.BLACK);
                specialized.setCardText("Imoen can't be blocked. Whenever Imoen deals combat damage to a player, you may exile an instant or sorcery card from your graveyard. If you do, create a 2/2 black Zombie creature token.");
            }
            case RED -> {
                specialized.setName("Imoen, Chaotic Trickster");
                specialized.setManaCost("{1}{U}{R}");
                specialized.setColors(List.of(CardColor.BLUE, CardColor.RED));
                specialized.setColorIdentity(List.of(CardColor.BLUE, CardColor.RED));
                specialized.setColor(CardColor.RED);
                specialized.setCardText("Imoen can't be blocked. Whenever Imoen deals combat damage to a player, you may exile an instant or sorcery card from your graveyard. If you do, Imoen deals 2 damage to each opponent.");
            }
            case GREEN -> {
                specialized.setName("Imoen, Wise Trickster");
                specialized.setManaCost("{1}{U}{G}");
                specialized.setColors(List.of(CardColor.BLUE, CardColor.GREEN));
                specialized.setColorIdentity(List.of(CardColor.BLUE, CardColor.GREEN));
                specialized.setColor(CardColor.GREEN);
                specialized.setCardText("Imoen can't be blocked. Whenever Imoen deals combat damage to a player, you may exile an instant or sorcery card from your graveyard. If you do, draw a card and you may play an additional land this turn.");
            }
            default -> throw new IllegalStateException("Unsupported Imoen specialization color: "
                    + specialize.color());
        }

        specialized.addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(ImoenTricksterFriend.specializedCombatDamageTrigger(specialize.color()),
                        "You may exile an instant or sorcery card from your graveyard."));
        source.exchangeCard(specialized);
    }
}
