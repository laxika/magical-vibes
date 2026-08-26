package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "210")
public class JudithCarnageConnoisseur extends Card {

    private static final String KEYWORD_MODE = "That spell gains deathtouch and lifelink";
    private static final String IMP_MODE =
            "Create a 2/2 red Imp creature token with \"When this token dies, it deals 2 damage to each opponent.\"";

    public JudithCarnageConnoisseur() {
        Map<EffectSlot, CardEffect> tokenEffects = Map.of(
                EffectSlot.ON_DEATH, new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )),
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(KEYWORD_MODE,
                                new GrantKeywordsToCastSpellEffect(Set.of(Keyword.DEATHTOUCH, Keyword.LIFELINK))),
                        new ChooseOneEffect.ChooseOneOption(IMP_MODE,
                                new CreateTokenEffect(1, "Imp", 2, 2, CardColor.RED,
                                        List.of(CardSubtype.IMP), Set.of(), Set.of(), tokenEffects))
                )))
        ));
    }
}
