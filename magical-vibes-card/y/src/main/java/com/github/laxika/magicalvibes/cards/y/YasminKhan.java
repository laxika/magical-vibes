package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "7")
@CardRegistration(set = "WHO", collectorNumber = "392")
@CardRegistration(set = "WHO", collectorNumber = "539")
@CardRegistration(set = "WHO", collectorNumber = "612")
@CardRegistration(set = "WHO", collectorNumber = "983")
@CardRegistration(set = "WHO", collectorNumber = "1130")
public class YasminKhan extends Card {

    public YasminKhan() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileTopCardMayPlayThisTurnEffect(1, false).until(Duration.UNTIL_YOUR_NEXT_END_STEP)),
                "{T}: Exile the top card of your library. Until your next end step, you may play it."
        ));
    }
}
