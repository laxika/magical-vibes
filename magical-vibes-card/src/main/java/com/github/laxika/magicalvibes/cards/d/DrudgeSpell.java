package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "45")
public class DrudgeSpell extends Card {

    public DrudgeSpell() {
        // 1/1 black Skeleton token with "{B}: Regenerate this token."
        CreateTokenEffect skeletonToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Skeleton", 1, 1, CardColor.BLACK, null,
                List.of(CardSubtype.SKELETON), Set.<Keyword>of(), Set.<CardType>of(), false, false, Map.of(),
                List.of(new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()), "{B}: Regenerate this token.")),
                false, false, false, 0, Set.<Keyword>of());

        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new ExileNCardsFromGraveyardCost(2, CardType.CREATURE), skeletonToken),
                "{B}, Exile two creature cards from your graveyard: Create a 1/1 black Skeleton creature token. "
                        + "It has \"{B}: Regenerate this token.\""));

        // Every Skeleton token, not just the ones this enchantment made.
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new DestroyAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsTokenPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.SKELETON))),
                true));
    }
}
